package servlet_package;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import dbinteract.DBUtil;

/**
 * Servlet implementation class updateProjServlet
 */
@WebServlet("/alter_proj")
public class updateProjServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private boolean isMultipart;
    private int maxFileSize = 1024 * 1024 * 10;
    private int maxMemSize = 100 * 1024;   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public updateProjServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String proj_id = "";
		String category = "";
		String country = "";
		String location = "";
		String deadline = "";
		String goal = "";
		String photo = "";
		String blurb = "";
		
		isMultipart = ServletFileUpload.isMultipartContent(request);
        String result = "";
        response.setContentType("text/html;charset=utf-8");
        if (!isMultipart) {
            result = "not available";
            response.getWriter().println(result);
            return;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(maxMemSize);
        String path = "G:/software/eclipse/workspace/db_proj/WebContent/uploaded_img/";
        factory.setRepository(new File(path));
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(maxFileSize);
		
        try {

            List fileItems = upload.parseRequest(request);
            Iterator i = fileItems.iterator();
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                if (!fi.isFormField()) {
                    String fieldName = fi.getFieldName();
                    String fileName = fi.getName();
                    String contentType = fi.getContentType();
                    boolean isInMemory = fi.isInMemory();
                    long sizeInBytes = fi.getSize();
                    String ctime=""+System.currentTimeMillis() / 1000;
                    File file = new File(path + ctime);
                    photo="uploaded_img/"+ctime;
                    fi.write(file);
                } else {
                	String foename=fi.getFieldName();
                	String con=fi.getString();
                	System.out.println(foename+","+con); 
                	if(foename.equals("proj_id"))proj_id=con;
                	if(foename.equals("category"))category=con;
                	if(foename.equals("country"))country=con;
                	if(foename.equals("location"))location=con;
                	if(foename.equals("deadline"))deadline=con;
                	if(foename.equals("goal"))goal=con;
                	if(foename.equals("blurb"))blurb=con;
                	
                	
                }
            
            }
            result = "succeed";
        } catch (Exception ex) {
            System.out.println("ex:" + ex.getMessage());
            result = "fail";
        }

		String temp[] = {proj_id,category,country,location,deadline,goal,photo,blurb};
		boolean success = DBUtil.alterProj(temp);
		if(success){
			request.getRequestDispatcher("project.jsp?"+"proj="+proj_id).forward(request,response);
		}
	}
}
