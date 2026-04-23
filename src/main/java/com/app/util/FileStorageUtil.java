package com.app.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorageUtil {

	@Value("${file.upload-dir}")
	private String uploadDir;

	// ================= SAVE FILE =================
	public String saveFile(MultipartFile file) throws IOException {

		if (file == null || file.isEmpty()) {
			throw new RuntimeException("File is empty");
		}

		// sanitize filename
		String originalName = file.getOriginalFilename().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9._-]", "");

		// unique filename
		String fileName = UUID.randomUUID().toString() + "_" + originalName;

		Path uploadPath = Paths.get(uploadDir);

		// create folder if not exists
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Path filePath = uploadPath.resolve(fileName);

		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		return fileName;
	}

	// ================= DELETE FILE =================
	public void deleteFile(String fileName) {

		if (fileName == null || fileName.isEmpty())
			return;

		try {
			Path filePath = Paths.get(uploadDir).resolve(fileName);
			Files.deleteIfExists(filePath);

		} catch (IOException e) {
			System.out.println("Failed to delete file: " + e.getMessage());
		}
	}
}