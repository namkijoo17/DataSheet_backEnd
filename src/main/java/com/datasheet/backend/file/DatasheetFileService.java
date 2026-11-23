package com.datasheet.backend.file;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.file.dto.DatasheetFileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DatasheetFileService {

	private final FileStorageProperties fileStorageProperties;
	private Path rootDir;

	@PostConstruct
	void init() throws IOException {
		rootDir = Paths.get(fileStorageProperties.getDatasheetDir()).toAbsolutePath().normalize();
		Files.createDirectories(rootDir);
	}

	public List<DatasheetFileResponse> listFiles() {
		if (!Files.isDirectory(rootDir)) {
			return List.of();
		}

		try (Stream<Path> stream = Files.list(rootDir)) {
			return stream
					.filter(Files::isRegularFile)
					.map(this::toResponse)
					.sorted(Comparator.comparing(DatasheetFileResponse::getFileName, String.CASE_INSENSITIVE_ORDER))
					.toList();
		} catch (IOException e) {
			throw new BusinessException(
					"FILE_LIST_FAILED",
					"데이터시트 목록을 조회하지 못했습니다.",
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	public Resource loadAsResource(String fileName) {
		Path file = resolveSafe(fileName);
		if (!Files.isRegularFile(file)) {
			throw new BusinessException(
					"FILE_NOT_FOUND",
					"파일을 찾을 수 없습니다: " + fileName,
					HttpStatus.NOT_FOUND
			);
		}

		try {
			Resource resource = new UrlResource(file.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new BusinessException(
						"FILE_NOT_FOUND",
						"파일을 읽을 수 없습니다: " + fileName,
						HttpStatus.NOT_FOUND
				);
			}
			return resource;
		} catch (MalformedURLException e) {
			throw new BusinessException(
					"FILE_NOT_FOUND",
					"파일을 찾을 수 없습니다: " + fileName,
					HttpStatus.NOT_FOUND
			);
		}
	}

	public String detectContentType(String fileName) {
		Path file = resolveSafe(fileName);
		try {
			String probed = Files.probeContentType(file);
			if (StringUtils.hasText(probed)) {
				return probed;
			}
		} catch (IOException ignored) {
			// fallback below
		}

		String lower = fileName.toLowerCase();
		if (lower.endsWith(".pdf")) {
			return MediaType.APPLICATION_PDF_VALUE;
		}
		if (lower.endsWith(".png")) {
			return MediaType.IMAGE_PNG_VALUE;
		}
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
			return MediaType.IMAGE_JPEG_VALUE;
		}
		return MediaType.APPLICATION_OCTET_STREAM_VALUE;
	}

	private Path resolveSafe(String fileName) {
		if (!StringUtils.hasText(fileName) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
			throw new BusinessException(
					"INVALID_FILE_NAME",
					"파일명이 올바르지 않습니다.",
					HttpStatus.BAD_REQUEST
			);
		}

		Path resolved = rootDir.resolve(fileName).normalize();
		if (!resolved.startsWith(rootDir)) {
			throw new BusinessException(
					"INVALID_FILE_NAME",
					"파일명이 올바르지 않습니다.",
					HttpStatus.BAD_REQUEST
			);
		}
		return resolved;
	}

	private DatasheetFileResponse toResponse(Path path) {
		try {
			return DatasheetFileResponse.builder()
					.fileName(path.getFileName().toString())
					.sizeBytes(Files.size(path))
					.contentType(detectContentType(path.getFileName().toString()))
					.lastModified(Files.getLastModifiedTime(path).toInstant())
					.build();
		} catch (IOException e) {
			return DatasheetFileResponse.builder()
					.fileName(path.getFileName().toString())
					.sizeBytes(0)
					.contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
					.lastModified(Instant.EPOCH)
					.build();
		}
	}
}
