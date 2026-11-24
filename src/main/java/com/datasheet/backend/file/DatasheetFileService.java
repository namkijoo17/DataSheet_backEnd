package com.datasheet.backend.file;

import com.datasheet.backend.common.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

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

	/** PDF 저장 후 실제 저장된 파일명 반환 */
	public String storePdf(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(
					"FILE_REQUIRED",
					"PDF 파일을 선택해주세요.",
					HttpStatus.BAD_REQUEST
			);
		}

		String original = StringUtils.cleanPath(
				file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
		);
		if (!StringUtils.hasText(original) || original.contains("..")) {
			throw new BusinessException(
					"INVALID_FILE_NAME",
					"파일명이 올바르지 않습니다.",
					HttpStatus.BAD_REQUEST
			);
		}

		String baseName = Paths.get(original).getFileName().toString();
		String lower = baseName.toLowerCase(Locale.ROOT);
		if (!lower.endsWith(".pdf")) {
			throw new BusinessException(
					"INVALID_FILE_TYPE",
					"PDF 파일만 업로드할 수 있습니다.",
					HttpStatus.BAD_REQUEST
			);
		}

		String contentType = file.getContentType();
		if (StringUtils.hasText(contentType)
				&& !contentType.equalsIgnoreCase(MediaType.APPLICATION_PDF_VALUE)
				&& !contentType.equalsIgnoreCase("application/octet-stream")) {
			throw new BusinessException(
					"INVALID_FILE_TYPE",
					"PDF 파일만 업로드할 수 있습니다.",
					HttpStatus.BAD_REQUEST
			);
		}

		String storedName = uniqueFileName(baseName);
		Path target = resolveSafe(storedName);
		try {
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			return storedName;
		} catch (IOException e) {
			throw new BusinessException(
					"FILE_STORE_FAILED",
					"파일 저장에 실패했습니다.",
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	public void deleteIfExists(String fileName) {
		if (!StringUtils.hasText(fileName)) {
			return;
		}
		try {
			Files.deleteIfExists(resolveSafe(fileName));
		} catch (IOException | BusinessException ignored) {
			// best-effort cleanup
		}
	}

	private String uniqueFileName(String baseName) {
		Path candidate = rootDir.resolve(baseName).normalize();
		if (!Files.exists(candidate)) {
			return baseName;
		}
		int dot = baseName.lastIndexOf('.');
		String stem = dot > 0 ? baseName.substring(0, dot) : baseName;
		String ext = dot > 0 ? baseName.substring(dot) : "";
		return stem + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
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
}
