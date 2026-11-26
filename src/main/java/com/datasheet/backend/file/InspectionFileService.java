package com.datasheet.backend.file;

import com.datasheet.backend.common.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** 검사 완료 결과 PDF 저장소 */
@Service
@RequiredArgsConstructor
public class InspectionFileService {

	private final FileStorageProperties fileStorageProperties;
	private Path rootDir;

	@PostConstruct
	void init() throws IOException {
		rootDir = Paths.get(fileStorageProperties.getInspectionDir()).toAbsolutePath().normalize();
		Files.createDirectories(rootDir);
	}

	/** PDF 저장 후 디스크에 저장된 고유 파일명 반환 */
	public String storePdf(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("FILE_REQUIRED", "결과 PDF 파일이 필요합니다.", HttpStatus.BAD_REQUEST);
		}
		if (!looksLikePdf(file)) {
			throw new BusinessException("INVALID_FILE_TYPE", "PDF 파일만 저장할 수 있습니다.", HttpStatus.BAD_REQUEST);
		}

		String storedName = UUID.randomUUID() + ".pdf";
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, resolveSafe(storedName), StandardCopyOption.REPLACE_EXISTING);
			return storedName;
		} catch (IOException e) {
			throw new BusinessException(
					"FILE_STORE_FAILED",
					"결과 파일 저장에 실패했습니다.",
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	public Resource load(String storedName) {
		Path file = resolveSafe(storedName);
		if (!Files.isRegularFile(file)) {
			throw new BusinessException("FILE_NOT_FOUND", "저장된 결과 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
		}
		try {
			Resource resource = new UrlResource(file.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new BusinessException("FILE_NOT_FOUND", "저장된 결과 파일을 읽을 수 없습니다.", HttpStatus.NOT_FOUND);
			}
			return resource;
		} catch (MalformedURLException e) {
			throw new BusinessException("FILE_NOT_FOUND", "저장된 결과 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
		}
	}

	public void deleteIfExists(String storedName) {
		if (!StringUtils.hasText(storedName)) {
			return;
		}
		try {
			Files.deleteIfExists(resolveSafe(storedName));
		} catch (IOException | BusinessException ignored) {
			// best-effort cleanup
		}
	}

	/** 파일 시그니처(%PDF)로 확인 */
	private boolean looksLikePdf(MultipartFile file) {
		try (InputStream in = file.getInputStream()) {
			byte[] head = in.readNBytes(5);
			return head.length == 5
					&& head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F' && head[4] == '-';
		} catch (IOException e) {
			return false;
		}
	}

	private Path resolveSafe(String fileName) {
		if (!StringUtils.hasText(fileName) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
			throw new BusinessException("INVALID_FILE_NAME", "파일명이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
		}
		Path resolved = rootDir.resolve(fileName).normalize();
		if (!resolved.startsWith(rootDir)) {
			throw new BusinessException("INVALID_FILE_NAME", "파일명이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
		}
		return resolved;
	}
}
