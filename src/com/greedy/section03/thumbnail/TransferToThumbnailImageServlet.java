package com.greedy.section03.thumbnail;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Servlet implementation class TransferToThumbnailImageServlet
 */
@WebServlet("/transferToThumbnail")
public class TransferToThumbnailImageServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * 썸네일 게시판 형태인 경우 원본 파일을 로드해서 사용자에게 보여주면 로딩도 느려지고,
		 * 모바일 접속 시 많은 양의 데이터 손실이 일어날 수 있다.
		 * 따라서 썸네일 게시판 이미지를 변환하여 보여주는 작업이 필요하다.
		 * --> Thumbnailator 라이브러리를 이용하여 변환 (https://mvnrepository.com/에서 다운)
		 */
		
		String originFilePath = request.getServletContext().getRealPath("/") + "resource/origin-image/flower1.PNG";
		File originFile = new File(originFilePath);
		String savedPath = request.getServletContext().getRealPath("/") + "resource/thumbnail-image/flower111.PNG";
		
		try {
			Thumbnails.of(originFile)
					  .size(150, 100)
					  .toFile(savedPath);
			System.out.println("썸네일 변환 성공!!");
			
		} catch(IOException e) {
			
		}
		
	}

}
