package com.greedy.section01.cos.multifileupload;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.greedy.common.UUIDFileRenamePolicy;
import com.oreilly.servlet.MultipartRequest;

@WebServlet("/cos/multi")
public class CosMultiFileUploadServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/* cos의 특징은 파일 업로드를 위해 단일용과 멀티용의 다른 소스를 모두 작성해주어야 한다. */
		
		/* 요청 헤더의 content-type이 multipart/form-date인 경우 true, 아닌 경우 false를 반환함  */
		if(ServletFileUpload.isMultipartContent(request)) {
			
			ServletContext context = request.getServletContext();
			String rootLocation = context.getInitParameter("upload-location");
			int maxFileSize = Integer.parseInt(context.getInitParameter("max-file-size"));
			String encodingType = context.getInitParameter("encoding-type");
			
			System.out.println("파일 저장 ROOT 경로 : " + rootLocation);
			System.out.println("최대 업로드 파일 용량 : " + maxFileSize);
			System.out.println("인코딩 방식 : " + encodingType);
			
			String multiFileUploadDirectory = rootLocation + "/cos/multi";
			
			File directory = new File(multiFileUploadDirectory);
			
			/* 파일 저장경로가 존재하지 않는 경우 디렉토리를 생성한다. */
			if(!directory.exists()) {
				/* 폴더를 한 개만 생성할거면 mkdir, 상위 폴더도 존재하지 않으면 한 번에 생성하란 의미로 mkdirs를 이용한다. */
				System.out.println("폴더 생성 : " + directory.mkdirs());
			}
			
			MultipartRequest multiRequest = new MultipartRequest(request, multiFileUploadDirectory, maxFileSize, encodingType, new UUIDFileRenamePolicy());
			
			/* multipart에서는 일반 request에서 파라미터로 꺼내오지 못하던 값도 꺼내올 수 있다. */
			String description = multiRequest.getParameter("description");
			System.out.println(description);
			
			/* 전송된 폼의 name속성값 */
			Enumeration<String> names = multiRequest.getFileNames();
			while(names.hasMoreElements()) {
				System.out.println(names.nextElement());
			}
			
			/* 여기까진 여러 개 올라간 파일을 알아서 자동으로 다 처리해준다. 
			 * 하지만 cos.jar는 2002년 이후로는 업데이트가 되지 않은 라이브러리이다.
			 * multiple 속성은 IE 10 이상부터 지원하며 업데이트 이후에 지원되던 속성이기 때문에 별도의 대안이 존재하지 않는다.
			 * 
			 * 따라서 모든 파일에 대한 정보를 확인할 수 없기 때문에 input type=file 태그를 여러 개 만들어서 개별적으로 업로드를 해야 했다.
			 * */
			
			/* 전송된 name값을 이용하여 폼으로 전송된 파일의 원본 이름을 리턴받음 */
			String originFileName = multiRequest.getOriginalFileName("multifile");
			System.out.println("form 전송된 파일의 원본 이름 : " + originFileName);
			
			/* 파일 이름 변경 후 파일시스템에 저장된 파일의 이름도 리턴받을 수 있다. */
			String changedFileName = multiRequest.getFilesystemName("multifile");
			System.out.println("파일 시스템에 저장된 변경된 파일의 이름 : " + changedFileName);
			
			/* 저장된 파일의 경로는 따로 불러올 수 없기 때문에 필요시에는 별도로 DB에 저장해두어야 한다. */
			String filePath = multiFileUploadDirectory;
			
			/* 비지니스 로직이 실패하는 경우 파일을 제거해 주어야 한다. */
			File remover = new File(multiFileUploadDirectory + "/" + changedFileName);
			
			System.out.println("파일 삭제 성공? : " + remover.delete());
			
		}
	}

}
