package com.greedy.section01.cos.manyfileupload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.greedy.common.UUIDFileRenamePolicy;
import com.oreilly.servlet.MultipartRequest;

/**
 * Servlet implementation class ManyFileUploadServlet
 */
@WebServlet("/cos/many")
public class CosManyFileUploadServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		/* 요청 헤더의 content-type이 multipart/form-date인 경우 true, 아닌 경우 false를 반환함  */
		if(ServletFileUpload.isMultipartContent(request)) {
			
			ServletContext context = request.getServletContext();
			String rootLocation = context.getInitParameter("upload-location");
			int maxFileSize = Integer.parseInt(context.getInitParameter("max-file-size"));
			String encodingType = context.getInitParameter("encoding-type");
			
			System.out.println("파일 저장 ROOT 경로 : " + rootLocation);
			System.out.println("최대 업로드 파일 용량 : " + maxFileSize);
			System.out.println("인코딩 방식 : " + encodingType);
			
			String manyFileUploadDirectory = rootLocation + "/cos/many";
			
			File directory = new File(manyFileUploadDirectory);
			
			/* 파일 저장경로가 존재하지 않는 경우 디렉토리를 생성한다. */
			if(!directory.exists()) {
				/* 폴더를 한 개만 생성할거면 mkdir, 상위 폴더도 존재하지 않으면 한 번에 생성하란 의미로 mkdirs를 이용한다. */
				System.out.println("폴더 생성 : " + directory.mkdirs());
			}
			
			MultipartRequest multiRequest = new MultipartRequest(request, manyFileUploadDirectory, maxFileSize, encodingType, new UUIDFileRenamePolicy());
			
			/* multipart에서는 일반 request에서 파라미터로 꺼내오지 못하던 값도 꺼내올 수 있다. */
			String description = multiRequest.getParameter("description");
			System.out.println(description);
			
			/* 폼으로 전송된 파일의 정보도 확인 가능하다. */
			
			/* 전송된 폼의 name속성값 */
			Enumeration<String> names = multiRequest.getFileNames();
			
			/* form의 name속성값을 임시로 저장해둘 ArrayList를 만든다. */
			List<String> nameList = new ArrayList<>();
			while(names.hasMoreElements()) {
				/* 폼의 순서 역순으로 출력된다. */
				String name = names.nextElement();
				System.out.println(name);
				nameList.add(name);
			}
			
			/* List의순서를 뒤집는다. */
			Collections.reverse(nameList);
			
			System.out.println(nameList);
			
			/* 원본 파일 이름과 파일 시스템 이름을 저장할 리스트가 각각 필요하다. */
			List<String> originFileNameList = new ArrayList<>();
			List<String> fileSystemNameList = new ArrayList<>();
			
			for(int i = 0; i < nameList.size(); i++) {
				originFileNameList.add(multiRequest.getOriginalFileName(nameList.get(i)));
				fileSystemNameList.add(multiRequest.getFilesystemName(nameList.get(i)));
			}
			
			System.out.println("원본 파일명 리스트 : " + originFileNameList);
			System.out.println("파일시스템명 리스트 : " + fileSystemNameList);
			
			/* 파일 삭제 시 모든 파일을 지워야 한다. */
			int cnt = 0;
			for(int i = 0; i < fileSystemNameList.size(); i++) {
				boolean isDeleted = new File(manyFileUploadDirectory + "/" + fileSystemNameList.get(i)).delete();
				if(isDeleted || fileSystemNameList.get(i) == null) {
					cnt += 1;
				}
			}
			
			if(fileSystemNameList.size() == cnt) {
				System.out.println("모든 파일 제거 성공");
			} else {
				System.out.println("모든 파일 제거 실패");
			}
			
			
		}
	}

}
