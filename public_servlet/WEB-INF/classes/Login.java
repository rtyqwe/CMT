import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Login extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
		//
		// Register MySQL Paper driver
		//
		try {
			// register the MySQL Paper driver with DriverManager
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//get HttpSession
		HttpSession session = request.getSession(true);
		//Clear previous login
		
		//
		// get the output stream for result page
		//
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		try {
			//
			// Connect to the database
			//

			Connection con = null;

			//
			// URL is jdbc:mysql:dbname
			// Change CS143 to the right database that you use
			//
			String url = "jdbc:mysql://localhost/cs143wch?zeroDateTimeBehavior=convertToNull";
			String userName = "cs143wch";
			String password = "9pvu4t2y";

			// connect to the database, user name and password can be specified
			// through this method
			con = DriverManager.getConnection(url, userName, password);

			//
			// Get operationID
			//
			String login_email = request.getParameter("email");
			String login_password= request.getParameter("password");

			String signup = request.getParameter("signup");
			String update = request.getParameter("update");
			String logout = request.getParameter("logout");
			
			String check_Chair="SELECT C.chair_id FROM Chair as C WHERE C.email = (SELECT email FROM Login_id as L where L.email ='"+login_email+"' AND L.password = '"+login_password+"')";
			PreparedStatement check_Chair_existence = con.prepareStatement(check_Chair);
			ResultSet  rs_Chair = check_Chair_existence.executeQuery();
			boolean rsC = rs_Chair.next();

			out.println("<html>");
			out.println("<head>");

			if(rsC==false){


				//Check if user info matches any Author or Reviewer
				String check_Author="SELECT A.author_id FROM Author as A WHERE A.email = (SELECT L.email FROM Login_id as L where L.email = '"+login_email+"' AND L.password = '"+login_password+"')";
				PreparedStatement check_Author_existence = con.prepareStatement(check_Author);
				ResultSet  rs_Author = check_Author_existence.executeQuery();
				String check_Reviewer="SELECT R.reviewer_id FROM Reviewer as R WHERE R.email = (SELECT L.email FROM Login_id as L where L.email = '"+login_email+"' AND L.password = '"+login_password+"')";
				PreparedStatement check_Reviewer_existence = con.prepareStatement(check_Reviewer);
				ResultSet  rs_Reviewer = check_Reviewer_existence.executeQuery();
				boolean rsA = rs_Author.next();
				boolean rsR = rs_Reviewer.next();
				if(rsA || rsR){
					String title = "Please select you role:";
					out.println("<title>" + title + "</title>");
					out.println("</head>");
					out.println("<body bgcolor=white>");
					out.println("<h1>" + title + "</h1>");
					out.println("<p><a href=Login?logout=true>Logout</a></p>");
					out.println("<p><a href=../update.html>Edit account info.</a></p>");
					out.println("<form method=POST action=../servlet/AuthorReviewer>");
					out.println("<!-- drop down box --><SELECT name=\"dropdown\"><OPTION SELECTED>Author<OPTION>Reviewer</SELECT><p><!-- submit button --><input type=\"submit\" value=\"Continue\">");
					if(rsA){
						out.println("<input type=hidden name=author_id value=\""+rs_Author.getInt(1)+"\">");
						out.println("<input type=hidden name=operationID value=\"0\">");
					}
					else
						out.println("<input type=hidden name=author_id value=\"\">");
					if(rsR){
						out.println("<input type=hidden name=reviewer_id value=\""+rs_Reviewer.getInt(1)+"\">");
						out.println("<input type=hidden name=operationID value=\"5\">");
					}
					else 
						out.println("<input type=hidden name=reviewer_id value=\"\">");
					
					out.println("</form> ");
					
					String role = (String)session.getAttribute("role");
					if (role == null) {
						if(rsA)
							role = new String("Author");
						else if(rsR)
							role = new String("Reviewer");
					} 

				}
				else if(logout!=null && logout.equals("true")){
					if(session != null){
						session.invalidate();
					}


					String title = "Logging out...";
					out.println("<title>" + title + "</title>");
					out.println("</head>");
					out.println("<body bgcolor=white>");
					out.println("<h1>" + title + "</h1>");
					out.println("<p></p><p><a href=.././login.html>Continue</a></p>");

				}
				
				else if(signup!=null && signup.equals("true")){
					String first_name = request.getParameter("first_name");
					String middle_name = request.getParameter("middle_name");
					String last_name = request.getParameter("last_name");
					String email = request.getParameter("email");
					String affiliation = request.getParameter("affiliation");
					String check_account="SELECT L.email FROM Login_id as L where L.email = '"+login_email+"'";

					PreparedStatement check_account_existence = con.prepareStatement(check_account);
					ResultSet  rs_account = check_account_existence.executeQuery();
					if(rs_account.next()){
						String title = "Account (email) existed!";
						out.println("<title>" + title + "</title>");
						out.println("</head>");
						out.println("<body bgcolor=white>");
						out.println("<h1>" + title + "</h1>");
						out.println("<p><a href=.././signup.html>Signup again.</a></p>");

					}
					else{
						String insert_Login_id = "INSERT INTO Login_id (email,username, password) VALUES ('"+email+"','"+email+"','"+last_name+"')";
						String insert_Author = "INSERT INTO Author (first_name, middle_name, last_name, email, affiliation) VALUES ('"+first_name+"','"+middle_name+"','"+last_name+"','"+email+"','"+affiliation+"')";
						String insert_Reviewer = "INSERT INTO Reviewer (first_name, middle_name, last_name, email, affiliation) VALUES ('"+first_name+"','"+middle_name+"','"+last_name+"','"+email+"','"+affiliation+"')";
						Statement stmt1 = con.createStatement();

						stmt1.executeUpdate(insert_Login_id);
						Statement stmt2 = con.createStatement();
						stmt2.executeUpdate(insert_Author);
						Statement stmt3 = con.createStatement();
						stmt3.executeUpdate(insert_Reviewer);
						String title = "Account createdl!";

						out.println("<title>" + title + "</title>");
						out.println("</head>");
						out.println("<body bgcolor=white>");

						out.println("<h1>" + title + "</h1>");
						out.println("<p>Welcome, "+first_name+ " "+last_name+"!</p>");
						out.println("<p></p><p><a href=.././login.html>Continue</a></p>");

					}

				}
				else if(update != null && update.equals("true")){
					String log_email = request.getParameter("log_email");
					String old_pwd = request.getParameter("old_password");
					String check_account="SELECT * FROM Login_id as L where L.email = '"+log_email+"' AND L.password = '"+old_pwd+"'";
					PreparedStatement check_account_existence = con.prepareStatement(check_account);
					ResultSet  rs_account = check_account_existence.executeQuery();
					String title = "Update account information";
					out.println("<title>" + title + "</title>");
					out.println("</head>");
					out.println("<body bgcolor=white>");
					out.println("<h1>" + title + "</h1>");
					
					if(log_email!=null && rs_account.next()){
						String new_pwd = request.getParameter("new_password");
						String re_new_pwd = request.getParameter("re_new_password");
						String new_fname = request.getParameter("first_name");
						String new_mname = request.getParameter("first_name");
						String new_lname = request.getParameter("last_name");
						String new_affiliation = request.getParameter("affiliation");
						if(!new_pwd.equals("")){
							if(!re_new_pwd.equals("")){
								if(new_pwd.equals(re_new_pwd)){
									String update_pwd = "UPDATE Login_id SET password = '"+new_pwd+"' WHERE email='"+log_email+"'";
									PreparedStatement update_account = con.prepareStatement(update_pwd);
									update_account.executeUpdate();
									out.println("<p>Password updated.</p>");
									out.println("<p><a href=../login.html>Please login again.</a><p>");
								}
								else{
									out.println("<p>New passwords do not match.</p>");
									out.println("<p><a href=../update.html>Try again</a><p>");
								}
							}

						}
						if(!new_fname.equals("")){
							String update_author_fname = "UPDATE Author SET first_name = '"+new_fname+"' WHERE email='"+log_email+"'";
							String update_reviewer_fname = "UPDATE Reviewer SET first_name = '"+new_fname+"' WHERE email='"+log_email+"'";
							PreparedStatement update_account = con.prepareStatement(update_author_fname);
							update_account.executeUpdate();
							update_account = con.prepareStatement(update_reviewer_fname);
							update_account.executeUpdate();
							out.println("<p>First name updated.</p>");
							out.println("<p><a href=../login.html>Please login again.</a><p>");
						}
						if(!new_mname.equals("")){
							String update_author_mname = "UPDATE Author SET middle_name = '"+new_mname+"' WHERE email='"+log_email+"'";
							String update_reviewer_mname = "UPDATE Reviewer SET middle_name = '"+new_mname+"' WHERE email='"+log_email+"'";
							PreparedStatement update_account = con.prepareStatement(update_author_mname);
							update_account.executeUpdate();
							update_account = con.prepareStatement(update_reviewer_mname);
							update_account.executeUpdate();
							out.println("<p>Middle name updated.</p>");
							out.println("<p><a href=../login.html>Please login again.</a><p>");
						}
						if(!new_lname.equals("")){
							String update_author_lname = "UPDATE Author SET last_name = '"+new_lname+"' WHERE email='"+log_email+"'";
							String update_reviewer_lname = "UPDATE Reviewer SET last_name = '"+new_lname+"' WHERE email='"+log_email+"'";
							PreparedStatement update_account = con.prepareStatement(update_author_lname);
							update_account.executeUpdate();
							update_account = con.prepareStatement(update_reviewer_lname);
							update_account.executeUpdate();
							out.println("<p>Last name updated.</p>");
							out.println("<p><a href=../login.html>Please login again.</a><p>");
						}
						if(!new_affiliation.equals("")){
							String update_author_affil = "UPDATE Author SET affiliation = '"+new_affiliation+"' WHERE email='"+log_email+"'";
							String update_reviewer_affil = "UPDATE Reviewer SET affiliation = '"+new_affiliation+"' WHERE email='"+log_email+"'";
							PreparedStatement update_account = con.prepareStatement(update_author_affil);
							update_account.executeUpdate();
							update_account = con.prepareStatement(update_reviewer_affil);
							update_account.executeUpdate();
							out.println("<p>Affiliation updated.</p>");
							out.println("<p><a href=../login.html>Please login again.</a><p>");
						}
					}
					else {
						out.println("<p>No such user.</p>");
						out.println("<p><a href=../update.html>Try again</a><p>");
					}
				}
				else{
					String title = "Error in Login...";
					out.println("<title>" + title + "</title>");
					out.println("</head>");
					out.println("<body bgcolor=white>");
					out.println("<h1>" + title + "</h1>");
					out.println("<p><a href=.././login.html>Sign-in again.</a></p>");
				}
				
				if (rsA)
					rs_Author.close();
				if (rsR)
					rs_Reviewer.close();
				if (rsC)
					rs_Chair.close();

			}
			else{

				String title = "Logged in as Chair.";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");
				out.println("<p><a href=../servlet/Chair?operationID=0&chair_id="+rs_Chair.getInt(1)+">Click here to continue.</a></p>");

				out.println("<p></p><p><a href=Login?logout=true>Logout</a></p>");

				String role = (String)session.getAttribute("role");
				if (role == null) {
					role = new String("Chair");
				} 
			}
		



	out.println("</body>");
	out.println("</html>");
	con.close();

} catch (SQLException ex) {
	out.println("SQLException caught<br>");
	out.println("---<br>");
	while (ex != null) {
		out.println("Message   : " + ex.getMessage() + "<br>");
		out.println("SQLState  : " + ex.getSQLState() + "<br>");
		out.println("ErrorCode : " + ex.getErrorCode() + "<br>");
		out.println("---<br>");
		ex = ex.getNextException();
	}
}
}
public void doGet(HttpServletRequest request,
	HttpServletResponse response)
throws ServletException, IOException {
	doPost(request, response);
}
}
