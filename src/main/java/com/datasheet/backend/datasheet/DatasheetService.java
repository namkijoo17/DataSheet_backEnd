package com.datasheet.backend.datasheet;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.datasheet.dto.DatasheetDetailResponse;
import com.datasheet.backend.datasheet.dto.DatasheetSummaryResponse;
import com.datasheet.backend.file.DatasheetFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatasheetService {

	private final DatasheetRepository datasheetRepository;
	private final DatasheetFileService datasheetFileService;

	@Transactional(readOnly = true)
	public List<DatasheetSummaryResponse> listActive() {
		return datasheetRepository.findByActiveTrueOrderByCodeAscRevisionDesc().stream()
				.map(DatasheetSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DatasheetDetailResponse getDetail(Long id) {
		Datasheet datasheet = datasheetRepository.findByIdWithItems(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		return DatasheetDetailResponse.from(datasheet);
	}

	@Transactional(readOnly = true)
	public Resource loadFile(Long id) {
		Datasheet datasheet = datasheetRepository.findById(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		return datasheetFileService.loadAsResource(datasheet.getFileName());
	}

	@Transactional(readOnly = true)
	public String contentType(Long id) {
		Datasheet datasheet = datasheetRepository.findById(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		return datasheetFileService.detectContentType(datasheet.getFileName());
	}

	@Transactional(readOnly = true)
	public String fileName(Long id) {
		return datasheetRepository.findById(id)
				.map(Datasheet::getFileName)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
	}
}
