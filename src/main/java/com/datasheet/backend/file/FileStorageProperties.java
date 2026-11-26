package com.datasheet.backend.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

	/** 데이터시트 원본 파일이 있는 로컬 폴더 (수동 배치) */
	private String datasheetDir = "./storage/datasheets";

	/** 검사 완료 결과 PDF 저장 폴더 */
	private String inspectionDir = "./storage/inspections";
}
