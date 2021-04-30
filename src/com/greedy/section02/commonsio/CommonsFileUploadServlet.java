package com.greedy.section02.commonsio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Servlet implementation class CommonsFileUploadServlet
 */
@WebServlet("/commons/single")
public class CommonsFileUploadServlet extends HttpServlet {
	
	private String rootLocation;
	private int maxFileSize;
	private String encodingType;

	@Override
	public void init() throws ServletException {
		// 웹 어플리케이션의 설정 값을 모두 가져옴 (뒤에 .찍어서 메소드 추가 가능) .getContext를 추가하여 root 경로를 확인할 수 있다.
		ServletContext context = getServletContext();
		// web.xml context-param 속성에 저정된 값들은 getInitParameter() 메소드를 이용하여 꺼낼 수 있다.
		
		rootLocation = context.getInitParameter("upload-location");
		maxFileSize = Integer.parseInt(context.getInitParameter("max-file-size"));
		encodingType = context.getInitParameter("encoding-type");
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// apache 것으로 추가 ** isMultipartConent()로 체크하는 것 꼭 기억해두기
		if(ServletFileUpload.isMultipartContent(request)) {
			
			System.out.println("파일 저장 ROOT 경로 : " + rootLocation);
			System.out.println("최대 파일 업로드 용량 : " + maxFileSize);
			System.out.println("인코딩 방식 : " + encodingType);
			
			// 파일 경로 지정
			String fileUploadDirectory = rootLocation + "/commons";
			// java.io로 임포트(파일 입출력)
			File directory = new File(fileUploadDirectory);
			
			// **exists() : 파일이 있는지 여부를 판단해서 있으면 true, 없으면 false로 반환
			// 파일 저장경로가 존재하지 않는 경우 디렉토리를 생성하자
			if(!directory.exists()) {
				/* 폴더를 한 개만 생성할꺼면 mkdir(), 상위폴더도 존재하지 않으면 한 번에 생성하라는 의미로 mkdirs()로 이용 */
				System.out.println("폴더 생성 : " + directory.mkdirs()); // 생성여부를 true, false로 리턴한다.
				
			}
			/*
			 * 최종적으로 request를 parsing하고 파일을 저장한 뒤 필요한 내용을 담을 리스트와 맵이다.
			 * 파일에 대한 정보를 리스트(fileList)에, 다른 파라미터의 정보는 모두 맵에 담을 것이다.
			 */
			
			/* Map은 파라미터값 담아주는 객체 */
			Map<String,String> parameter = new HashMap<>();
			/* 파일에 있는 인덱스를 모두 담아주고(1개 이상 파일을 담을 수 있음) 인덱스에 모든 설정 값을 다 담아준다.(key,value)형태 */
			List<Map<String,String>> fileList = new ArrayList<>();
		
			/* 파일을 업로드 할 때 최대 크기나 임시 저장할 폴더의 경로 등을 포함하기 위한 인스턴스 생성 */
			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
			// Commons로 임포트
			fileItemFactory.setRepository(new File(fileUploadDirectory));
			// 파일 업로드 위치 지정
			fileItemFactory.setSizeThreshold(maxFileSize);
			// 사이즈 지정
		
			/* 서블릿에서 기본 제공하는 인스턴스 말고 꼭 Commons fileUpload 라이브러리에 있는 클래스로 임포트 해야 한다.*/
			ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
			
			// 파일과 관련있으니 예외처리 해주자 (최상위 객체인 Exception 사용)
			try {
				/* request를 파싱하여 데이터 하나 하나를 FileItem 인스턴스로 반환한다. */
				// Commons로 import and 
				List<FileItem> fileItems = fileUpload.parseRequest(request);
				// **request에 담겨온 파일과 form tag(text)를 모두 받아서,
				// **아래에서 나눠서(분기처리) 쓰겠다.
				// **업로드된 파일을 temp에서 임시저장 했다가 모두 처리되면 업로드 한다. (cos랑 상반됨)
				
				for(FileItem item : fileItems) {				
					/* 폼 데이터는 isFormedField 속성이 true이고, 파일은 isFormedField 속성이 false이다. */
					System.out.println(item); // isFormedField가 false: file이다. true: file이 아니다.

				}
				// **list 형태이니 length()가 아닌 size() 사용, get()을 사용하여 인덱스 값 가져옴
				for(int i = 0; i < fileItems.size(); i++) {
					
					FileItem item = fileItems.get(i); // 참고, 배열은 배열명[index]로 리스트는 인덱스를 get() 메소드로 찾아온다.
					
					// 분기 처리
					if(!item.isFormField()) { // 파일 데이터인 경우
						
						if(item.getSize() > 0) {
							/*
							 * 파일의 사이즈가 0보다 커야 전송된 파일이 있다는 의미
							 * 전송된 파일이 있는 경우에만 처리하고, 0인 경우에는 무시하자
							 */
							String fieldName = item.getFieldName(); // 속성이름(singlefile..)
							String originFileName = item.getName(); // 파일명
							
							/*
							 * 업로드 되는 파일명 변경작업 하는 이유
							 * 1. 동일한 파일명이 들어왔을 때 대비하기 위해
							 * 2. 특수문자 또는 입력받기 힘든 문자를 받았을 때를 대비하기 위해
							 * 
							 */
							//System.out.println("fieldName : " + fieldName);
							//System.out.println("originFileName : " + originFileName);
							
							int dot = originFileName.lastIndexOf("."); // 마지막 점 위치 찾아오는 것
							String ext = originFileName.substring(dot); // .부터 다 가져온다.
							
							System.out.println("dot : " + dot);
							System.out.println("ext : " + ext);
							
							// 파일명을 바꿔주는 형태 (UUID
							String randomFileName = UUID.randomUUID().toString().replace("-", "") + ext;
							
							/* 저장된 파일 정보를 담은 인스턴스를 생성하고 */
							File storeFile = new File(fileUploadDirectory + "/" + randomFileName);
							
							// 삭제 테스트를 위해 강제로 에러 발생 (index out of bounds)
							//String str = "";
							//System.out.println(str.charAt(1));
							
							/* 저장한다. */
							item.write(storeFile);
							
							/* 필요한 정보를 Map에 담는다. */
							// **파일 업로드시 가장 기본이 되는 요소들
							Map<String, String> fileMap = new HashMap<>();
							fileMap.put("fileName", fieldName);
							fileMap.put("originFileName", originFileName);
							fileMap.put("randomFileName", randomFileName);
							fileMap.put("savePath", fileUploadDirectory);
							
							fileList.add(fileMap);
						}
	 					
					} else { // 폼 데이터인 경우
						/*
						 * 전송된 폼의 name은 getFieldName()으로 받아오고,
						 * 해당 필드의 value는 getString()으로 받아온다.
						 * 인코딩 설정을 하더라도 전송되는 파라미터는 ISO-8859-1로 처리된다.
						 * 별도로 ISO-8859-1로 해석된 한글을 UTF-8로 변경해주어야 한다.
						 */
						// toString()을 사용하면 한글이 깨진다
						//parameter.put(item.getFieldName(), item.getString());
						// getByte()로 받아와서 new String을 사용하여 UTF-8로 인코딩한다.
						parameter.put(item.getFieldName(), new String(item.getString().getBytes("ISO-8859-1"),"UTF-8"));
					
					}
				}
				// console값을 보면 파일 하나의 정보가 뭉쳐서 들어간다.
				// ==> List 형태의 전송
				System.out.println("parameter : " + parameter);
				System.out.println("fieldList : " + fileList);
				
			} catch(Exception e) {
				
				/* 어떤 종류의 Exception이 발생해서 실패를 하더라도 파일을 삭제해야 한다. */
				int cnt = 0;
				for(int i = 0; i <fileList.size(); i++) {
					
					Map<String,String> file = fileList.get(i);
					
					File deleteFile = new File(fileUploadDirectory + "/" + file.get("savedFileName"));
					boolean isDeleted = deleteFile.delete();
					
					if(isDeleted) {
						cnt++;
					}
				}
				
				if(cnt == fileList.size()) {
					System.out.println("업로드에 실패한 모든 사진 삭제완료!");
				} else {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
