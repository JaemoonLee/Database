package servlet_package;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dbinteract.DBUtil;

/**
 * Servlet implementation class back_proj
 */
@WebServlet("/back_proj")
public class back_proj extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public back_proj() {
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
		String proj_id=request.getParameter("proj_id");
		String username=(String) request.getSession().getAttribute("username");
		String amount=request.getParameter("amount");
		boolean isSuccess=DBUtil.backProj(proj_id, username, amount);
		if(isSuccess){
			request.getRequestDispatcher("personal_center.jsp").forward(request,response);
		} 
	}

}
