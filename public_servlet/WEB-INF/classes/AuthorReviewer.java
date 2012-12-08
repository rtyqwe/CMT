import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AuthorReviewer extends HttpServlet {

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
			// Get user info
			//
			String author_id;
			//session.setAttribute("author_id", request.getParameter("author_id"));
			//if(!session.isNew())
			author_id = (String) session.getAttribute("author_id");
			if(author_id == null)
				author_id = request.getParameter("author_id");

			String reviewer_id;
			//session.setAttribute("reviewer_id", request.getParameter("reviewer_id"));
			//if(!session.isNew())
			reviewer_id= (String) session.getAttribute("reviewer_id");
			if(reviewer_id == null)
				reviewer_id =  request.getParameter("reviewer_id");
			
			String selected_role;
			//session.setAttribute("selected_role", request.getParameter("dropdown"));
			//if(!session.isNew())
			selected_role = (String) session.getAttribute("selected_role");
			if(selected_role == null)
				selected_role = request.getParameter("dropdown");

			String selected_paper;
			//session.setAttribute("selected_role", request.getParameter("dropdown"));
			//if(!session.isNew())
			selected_paper = (String) session.getAttribute("selected_paper");
			if(selected_paper == null)
				selected_paper = request.getParameter("selected_paper");
			
			String author_name="";
			String reviewer_name="";

			String temp_opID = request.getParameter("operationID");
			int operationID=-1;
			//if(!temp_opID.equals(""))
				operationID = Integer.parseInt(temp_opID);

			out.println("<html>");
			out.println("<head>");

			
			if(selected_role.equals("Author")){
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT first_name, middle_name, last_name FROM Author WHERE author_id ="+author_id);
				String title="";

				if(rs.next()){
					if(rs.getString(2)!=null)
						author_name = rs.getString(1)+ " "+rs.getString(2)+" "+rs.getString(3);
					else
						author_name = rs.getString(1)+" "+rs.getString(3);
					title = "Welcome "+author_name+".";
				}
				else{
					title = "You are not an author to any paper in any conference...";
					operationID = -1;
				}
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");
				out.println("<p><a href=Login?logout=true>Logout</a></p>");
				out.println("<p><a href=../update.html>Edit account info.</a></p>");
				if(operationID == 0){
					out.println("<p></p><p>Actions:</a></p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=1\">See list of all papers for all conferences authored by you</a><p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=2\">Create or Edit a paper submission for a conference</a><p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=3\">Add co-author information for a paper you authored</a><p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=4\">See a paper's final decision and Reviews</a><p>");
				}
				else if(operationID == 1){
					String get_Paperlist="SELECT paper_id, title, authors FROM Paper where paper_id in (SELECT paper_id FROM authored WHERE author_id = "+author_id+")";
					PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
					ResultSet  rs_Papers = PaperList_statement.executeQuery();
					out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
					out.println("<p>Papers authored by you:</p>");
					
					out.println("<table border=\"1\">");
					out.println("<tr><th>Paper ID</th><th>Title</th><th>Authors</th><th>Approve Status</th></tr>");
					while(rs_Papers.next()){
						int paper_id = rs_Papers.getInt(1);
						String paper_title = rs_Papers.getString(2);
						String authors = rs_Papers.getString(3);
						String approve_status="";
						String check_status = "SELECT C.process_over FROM Conference as C WHERE C.conf_id = (SELECT conf_id FROM paper_in_conf WHERE paper_id ="+paper_id+")";
						PreparedStatement CheckStatus_statement = con.prepareStatement(check_status);
						ResultSet  rs_check_status = CheckStatus_statement.executeQuery();
						double rating = 0;

						if(rs_check_status.next()){
							if((rs_check_status.getString(1)).equals("0")){
								approve_status = "In review";
								out.println("<tr><td>" + paper_id + "</td><td>"+ paper_title+"</td><td>"+authors+"</td><td>"+approve_status+"</td></tr>");
							}
							else{
								String cal_rating = "SELECT * FROM reviewed WHERE paper_id ="+Integer.toString(paper_id);
								PreparedStatement CalRating_statement = con.prepareStatement(cal_rating);
								ResultSet  rs_cal_rating = CalRating_statement.executeQuery();
								rating = 0.0;
								int i = 0;
								while(rs_cal_rating.next()){
									i++;
									rating+=rs_cal_rating.getInt(4);
								}
								if(i!=0)
									rating = rating/i;
								else
									rating = rating;
								if(rating > 4.0)
									approve_status = "Accept";
								else
									approve_status = "Reject";

								out.println("<tr><td>" + paper_id+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td><td>"+approve_status+"</td></tr>");

							}
						}
					}
				}
				else if (operationID == 2){
					out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
					if(selected_paper==null){
						//String get_conferences = "SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP();";
						String get_Paperlist="SELECT DISTINCT P.paper_id, P.title, P.authors, P.abstract, C.conf_id FROM Paper as P, paper_in_conf as C WHERE P.paper_id IN  (SELECT paper_id FROM authored WHERE author_id = "+author_id+") AND C.conf_id IN (SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP())";


						PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
						ResultSet  rs_Papers = PaperList_statement.executeQuery();
						out.println("<p><a href=\"./AuthorReviewer?operationID=2&selected_paper=new\">Add new paper</a></p>");
						out.println("<p>Papers that you may edit:</p>");

						out.println("<table border=\"1\">");
						out.println("<tr><th></th><th>Paper ID</th><th>Title</th><th>Authors</th><th>Abstract</th><th>Conference ID</th></tr>");
						while(rs_Papers.next()){
							int paper_id = rs_Papers.getInt(1);
							String paper_title = rs_Papers.getString(2);
							String authors = rs_Papers.getString(3);
							String abstract_text = rs_Papers.getString(4);
							String conf_id = rs_Papers.getString(5);

							out.println("<tr><td><a href=\"./AuthorReviewer?operationID=2&selected_paper="+Integer.toString(paper_id)+"\">Edit</a></td><td>" + Integer.toString(paper_id)+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td><td>"+abstract_text+"</td><td>"+conf_id+"</td></tr>");

						}
					}
					else if(selected_paper.equals("new")){
						String get_conference = "SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP()";
						ResultSet rs_conf = stmt.executeQuery(get_conference);
						String list_conf = "<select name=\"dropdown\"><OPTION SELECTED>";
						int i = 0;
						while(rs_conf.next()){
							if(i==0)
								list_conf = list_conf+rs_conf.getString(1);
							else
								list_conf = list_conf+"<OPTION>"+rs_conf.getString(1);
							i++;
						}
						list_conf = list_conf + "</select>";
						out.println("<form method=post action=../servlet/AuthorReviewer>");
						out.println("<h3> Add New Paper</h3>");
						out.println("<p>");
						out.println("<label for=\"confid_str\">For Conference:</label><br>");
						out.println(list_conf);
						out.println("</p>");
						out.println("<p>");
						out.println("<label for=\"title_str\">Title:</label><br>");
						out.println("<input type=text name=title_str>");
						out.println("</p>");
						out.println("<p>");
						out.println("<label for=\"abstract_str\">Abstract:</label></br>");
						out.println("<textarea name=\"abstract_str\" cols=\"100\" rows=\"20\" maxlength=2000></textarea></br>");
						out.println("<small>Character limit: 2000</small>");
						out.println("</p>");
						out.println("<p>");
						out.println("<label for=\"content_str\">Content:</label></br>");
						out.println("<textarea name=\"content_str\" cols=\"80\" rows=\"70\" maxlength=5000></textarea></br>");
						out.println("<small>Character limit: 5000</small>");
						out.println("</p>");
						out.println("<input type=\"hidden\" name=\"operationID\" value=\"2\">");
						out.println("<input type=\"hidden\" name=\"selected_paper\" value=\"addnew\">");
						out.println("<p> <input type=submit name=\"paper_submit\" value=\"Submit\"> </p></form>");
					//	out.println("<p><a href=\"AuthorReviewer?operationID=2\">Go back to list</a></p>");
					}
					else if(selected_paper.equals("addnew")){
						con.setAutoCommit(false);
						String title_str = request.getParameter("title_str");
						String abstract_str = request.getParameter("abstract_str");
						String content_str = request.getParameter("content_str");
						int selected_confid = Integer.parseInt(request.getParameter("dropdown"));
						int paper_key=0;
						String insert_paper = "INSERT INTO Paper (title, authors, abstract, content) VALUES('"+title_str+"','"+author_name+"','"+abstract_str+"','"+content_str+"')";
						PreparedStatement insert_paper_stmt = con.prepareStatement(insert_paper, Statement.RETURN_GENERATED_KEYS);
						insert_paper_stmt.executeUpdate();
						ResultSet generatedKeys = insert_paper_stmt.getGeneratedKeys();
						while (generatedKeys.next()) {
							paper_key = generatedKeys.getInt(1);
						}
						String insert_authored = "INSERT INTO authored (author_id, paper_id) VALUES("+author_id+","+Integer.toString(paper_key)+")";
						PreparedStatement insert_authored_stmt = con.prepareStatement(insert_authored);
						insert_authored_stmt.executeUpdate();
						String insert_conf = "INSERT INTO paper_in_conf (conf_id, paper_id) VALUES("+Integer.toString(selected_confid)+","+Integer.toString(paper_key)+")";
						PreparedStatement insert_conf_stmt = con.prepareStatement(insert_conf);
						insert_conf_stmt.executeUpdate();
						con.commit();
						con.setAutoCommit(true);
						out.println("<p>Paper successfully added!</p>");
					}
					else if(selected_paper.equals("update")){
						String updated_paper_id = request.getParameter("updated_paper");
						String title_str = request.getParameter("title_str");
						String abstract_str = request.getParameter("abstract_str");
						String content_str = request.getParameter("content_str");
						String update_paper = "UPDATE Paper SET title ='"+title_str+"', abstract='"+abstract_str+"', content='"+content_str+"' WHERE paper_id = "+updated_paper_id;
						PreparedStatement update_paper_stmt = con.prepareStatement(update_paper);
						update_paper_stmt.executeUpdate();
						out.println("<p>Paper successfully updated!</p>");
					}
					else{
					//if the Author chose a paper that is before submission deadline
						String get_Paper="SELECT DISTINCT P.title, P.abstract, P.content, P.paper_id FROM Paper as P WHERE P.paper_id ="+selected_paper;
						PreparedStatement get_paper_stmt = con.prepareStatement(get_Paper);
						ResultSet rs_paper = get_paper_stmt.executeQuery();
						if(rs_paper.next()){
							String title_str = rs_paper.getString(1);
							
							String abstract_str = rs_paper.getString(2);
							String content_str = rs_paper.getString(3);
							int paper_id = rs_paper.getInt(4);

							out.println("<form method=post action=../servlet/AuthorReviewer>");
							out.println("<h3> Edit Paper \""+title_str+"\":</h3>");
							out.println("<p>");
							out.println("<label for=\"title_str\">Title:</label><br>");
							out.println("<textarea name=\"title_str\" cols=\"100\" rows=\"1\" maxlength=100>"+title_str+"</textarea></br>");
							out.println("</p>");
							out.println("<p>");
							out.println("<label for=\"abstract_str\">Abstract:</label></br>");
							out.println("<textarea name=\"abstract_str\" cols=\"100\" rows=\"20\" maxlength=2000>"+abstract_str+"</textarea></br>");
							out.println("<small>Character limit: 2000</small>");
							out.println("</p>");
							out.println("<p>");
							out.println("<label for=\"content_str\">Content:</label></br>");
							out.println("<textarea name=\"content_str\" cols=\"80\" rows=\"70\" maxlength=5000>"+content_str+"</textarea></br>");
							out.println("<small>Character limit: 5000</small>");
							out.println("</p>");
							out.println("<input type=\"hidden\" name=\"operationID\" value=\"2\">");
							out.println("<input type=\"hidden\" name=\"updated_paper\" value=\""+Integer.toString(paper_id)+"\">");
							out.println("<input type=\"hidden\" name=\"selected_paper\" value=\"update\">");
							out.println("<p> <input type=submit name=\"paper_submit\" value=\"Submit\"> </p></form>");
					//	out.println("<p><a href=\"AuthorReviewer?operationID=2\">Go back to list</a></p>");
						}


					}

					
				}
				else if (operationID == 3){
					out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
					if(selected_paper==null){
						//String get_conferences = "SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP();";
						String get_Paperlist="SELECT DISTINCT P.paper_id, P.title, P.authors, P.abstract, C.conf_id FROM Paper as P, paper_in_conf as C WHERE P.paper_id IN  (SELECT paper_id FROM authored WHERE author_id = "+author_id+") AND C.conf_id IN (SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP())";


						PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
						ResultSet  rs_Papers = PaperList_statement.executeQuery();
						out.println("<p><a href=\"./AuthorReviewer?operationID=2&selected_paper=new\">Add new paper</a></p>");
						out.println("<p>Papers that you may add coauthors:</p>");

						out.println("<table border=\"1\">");
						out.println("<tr><th></th><th>Paper ID</th><th>Title</th><th>Authors</th><th>Abstract</th><th>Conference ID</th></tr>");
						while(rs_Papers.next()){
							int paper_id = rs_Papers.getInt(1);
							String paper_title = rs_Papers.getString(2);
							String authors = rs_Papers.getString(3);
							String abstract_text = rs_Papers.getString(4);
							String conf_id = rs_Papers.getString(5);

							out.println("<tr><td><a href=\"./AuthorReviewer?operationID=3&selected_paper="+Integer.toString(paper_id)+"\">Edit</a></td><td>" + Integer.toString(paper_id)+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td><td>"+abstract_text+"</td><td>"+conf_id+"</td></tr>");

						}
					}
					else if(selected_paper.equals("update")){
						con.setAutoCommit(false);
						String coauthor_email = request.getParameter("coauthor_email");
						String updated_paper_id = request.getParameter("updated_paper");
						String check_coauthor = "SELECT L.email FROM Login_id as L where L.email = '"+coauthor_email+"'";
						PreparedStatement check_coauthor_stmt = con.prepareStatement(check_coauthor);
						ResultSet rs_coauthor = check_coauthor_stmt.executeQuery();
						if(rs_coauthor.next()){
							//Check duplicate coauthors 
							String check_duplicate = "SELECT * FROM authored WHERE author_id IN (SELECT DISTINCT author_id from Author WHERE email='"+coauthor_email+"') AND paper_id = "+updated_paper_id;
							PreparedStatement check_duplicate_stmt = con.prepareStatement(check_duplicate);
							ResultSet rs_duplicate = check_duplicate_stmt.executeQuery();
							if(rs_duplicate.next()){
								out.println("<p>Error! He/She is already an author for the selected paper!</p>");
							}
							else{
								String get_coauthorid = "SELECT DISTINCT author_id from Author WHERE email='"+coauthor_email+"'";
								PreparedStatement get_coauthorid_stmt = con.prepareStatement(get_coauthorid);
								ResultSet rs_coauthorid = get_coauthorid_stmt.executeQuery();
								String coauthorid = "";
								if(rs_coauthorid.next()){
									coauthorid = rs_coauthorid.getString(1);
								}

								String get_cocoauthors = "SELECT author_id FROM authored WHERE paper_id="+updated_paper_id;
								PreparedStatement get_cocoauthors_stmt = con.prepareStatement(get_cocoauthors);
								ResultSet rs_cocoauthors = get_cocoauthors_stmt.executeQuery();
								ArrayList<String> cocoauthors = new ArrayList<String>();
								while(rs_cocoauthors.next()){
									cocoauthors.add(rs_cocoauthors.getString(1));
								}
								for(int i = 0; i < cocoauthors.size();i++){
									String insert_coauthors1 = "INSERT INTO coauthored (author1_id, author2_id, paper_id) VALUES("+cocoauthors.get(i)+","+coauthorid+","+updated_paper_id+")";
									PreparedStatement insert_coauthors1_stmt = con.prepareStatement(insert_coauthors1);
									insert_coauthors1_stmt.executeUpdate();
									String insert_coauthors2 = "INSERT INTO coauthored (author2_id, author1_id, paper_id) VALUES("+cocoauthors.get(i)+","+coauthorid+","+updated_paper_id+")";
									PreparedStatement insert_coauthors2_stmt = con.prepareStatement(insert_coauthors2);
									insert_coauthors2_stmt.executeUpdate();
								}
								String insert_authored = "INSERT INTO authored (author_id, paper_id) VALUES("+coauthorid+", "+updated_paper_id+")";
								PreparedStatement insert_authored_stmt = con.prepareStatement(insert_authored);
								insert_authored_stmt.executeUpdate();
								String get_newcoauthors = "SELECT DISTINCT A.first_name, A.middle_name, A.last_name FROM Author as A, authored as B WHERE A.author_id = B.author_id AND B.paper_id="+updated_paper_id;
								PreparedStatement get_newcoauthors_stmt = con.prepareStatement(get_newcoauthors);
								ResultSet rs_newcoauthors = get_newcoauthors_stmt.executeQuery();
								String newcoauthors ="";
								int j = 0;
								while(rs_newcoauthors.next()){
									if(j==0){
										if(rs_newcoauthors.getString(2)!=null)
											newcoauthors = rs_newcoauthors.getString(1)+ " "+rs_newcoauthors.getString(2)+ " "+rs_newcoauthors.getString(3);
										else 
											newcoauthors = rs_newcoauthors.getString(1)+" "+rs_newcoauthors.getString(3);
									}
									else{
										if(rs_newcoauthors.getString(2)!=null)
											newcoauthors = newcoauthors+", "+ rs_newcoauthors.getString(1)+ " "+rs_newcoauthors.getString(2)+ " "+rs_newcoauthors.getString(3);
										else 
											newcoauthors = newcoauthors+", "+rs_newcoauthors.getString(1)+" "+rs_newcoauthors.getString(3);
									}

									j++;
								}
								String update_Paper = "UPDATE Paper SET authors = '"+newcoauthors+"' WHERE paper_id="+updated_paper_id;
								PreparedStatement update_Paper_stmt = con.prepareStatement(update_Paper);
								update_Paper_stmt.executeUpdate();
								out.println("<p>Sucessfully added Coauthor with email - "+coauthor_email+".</p>");
							}
						}
						else{
							out.println("<p>Error! Email does not match any user in system!</p>");
						}
						con.commit();

						con.setAutoCommit(false);
					}
					else{
						
						out.println("<p>Add a coauthor (enter email address):</p>");
						out.println("<form method=post action=../servlet/AuthorReviewer>");
						out.println("<p><input type=text name=coauthor_email></p>");
						out.println("<input type=\"hidden\" name=\"operationID\" value=\"3\">");
						//out.println(selected_paper);
						out.println("<input type=\"hidden\" name=\"updated_paper\" value=\""+selected_paper+"\">");
						out.println("<input type=\"hidden\" name=\"selected_paper\" value=\"update\">");
						out.println("<p> <input type=submit name=\"coauthor_submit\" value=\"Submit\"> </p></form>");
					}

				}
				else if(operationID == 4){

					if(selected_paper==null){
						String get_Paperlist="SELECT DISTINCT paper_id, title, authors FROM Paper where paper_id in (SELECT paper_id FROM authored WHERE author_id = "+author_id+")";
						PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
						ResultSet  rs_Papers = PaperList_statement.executeQuery();
						out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
						out.println("<p>Papers authored by you:</p>");
						out.println("<table border=\"1\">");
						out.println("<tr><th></th><th>Paper ID</th><th>Title</th><th>Authors</th></tr>");
						while(rs_Papers.next()){
							int paper_id = rs_Papers.getInt(1);
							String paper_title = rs_Papers.getString(2);
							String authors = rs_Papers.getString(3);

					//		out.println("<tr><td><a href=\"./AuthorReviewer?operationID=4&selected_paper="+paper_id+"\">Select</a></td><td>"+paper_id+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td></tr>");
	//String approve_status="";
							String check_status = "SELECT C.process_over FROM Conference as C WHERE C.conf_id = (SELECT conf_id FROM paper_in_conf WHERE paper_id ="+paper_id+")";
							PreparedStatement CheckStatus_statement = con.prepareStatement(check_status);
							ResultSet  rs_check_status = CheckStatus_statement.executeQuery();
							double rating = 0;

							if(rs_check_status.next()){
								if((rs_check_status.getString(1)).equals("1")){
									out.println("<tr><td><a href=\"./AuthorReviewer?operationID=4&selected_paper="+paper_id+"\">Select</a></td><td>"+paper_id+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td></tr>");
								}
							}
						}
					}
					else{
						String get_title = "SELECT title FROM Paper WHERE paper_id="+selected_paper;
						PreparedStatement get_title_statement = con.prepareStatement(get_title);
						ResultSet  rs_get_title_status = get_title_statement.executeQuery();
						rs_get_title_status.next();
						String selected_title=rs_get_title_status.getString(1);
						out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
						out.println("<p>Final decision and reviews for <b>\""+selected_title+"\"</b></p>");
						out.println("<table border=\"1\">");
						out.println("<tr><th>Reviewer ID</th><th>Overall Rating</th><th>Comments</th></tr>");
						String cal_rating = "SELECT reviewer_id, overall_rating, comments FROM reviewed WHERE paper_id ="+selected_paper;
						PreparedStatement CalRating_statement = con.prepareStatement(cal_rating);
						ResultSet  rs_cal_rating = CalRating_statement.executeQuery();
						double rating = 0.0;
						String approve_status = "";
						int i = 0;
						while(rs_cal_rating.next()){
							i++;
							rating+=rs_cal_rating.getInt(2);
							int selected_reviewer_id = rs_cal_rating.getInt(1);
							int overall_rating = rs_cal_rating.getInt(2);
							String comments = rs_cal_rating.getString(3);
							out.println("<tr><td>" + Integer.toString(selected_reviewer_id)+ "</td><td>"+ Integer.toString(overall_rating)+"</td><td>"+comments+"</td></tr>");
						}
						if(i!=0)
							rating = rating/i;
						else
							rating = rating;
						if(rating > 4.0)
							approve_status = "Accept";
						else
							approve_status = "Reject";

						out.println("<p>Final decision of paper: <b>"+approve_status+"</b></p>");


					}
				}

			}
			else if (selected_role.equals("Reviewer")){
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT first_name, middle_name, last_name FROM Reviewer WHERE reviewer_id ="+reviewer_id);
				String title="";

				if(rs.next()){
					if(rs.getString(2)!=null)
						reviewer_name = rs.getString(1)+ " "+rs.getString(2)+" "+rs.getString(3);
					else
						reviewer_name = rs.getString(1)+" "+rs.getString(3);
					title = "Welcome "+reviewer_name+".";
				}
				else{
					title = "You are not a reviewer to any paper in any conference...";
					operationID = -1;
				}
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				out.println("<h1>" + title + "</h1>");
				out.println("<p><a href=Login?logout=true>Logout</a></p>");
				out.println("<p><a href=../update.html>Edit account info.</a></p>");
				if(operationID == 0){
					out.println("<p></p><p>Actions:</a></p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=6\">See list of all papers for all conferences (As Reviewer)</a><p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=7\">Read basic information of assigned paper(s)</a><p>");
					out.println("<p><a href=\"./AuthorReviewer?operationID=8\">Submit review for assigned paper(s)</a><p>");
				}
				if(operationID==6){
					String get_Paperlist="SELECT paper_id, title, authors FROM Paper where paper_id in (SELECT paper_id FROM reviewed WHERE reviewer_id = "+reviewer_id+")";
					PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
					ResultSet  rs_Papers = PaperList_statement.executeQuery();
					out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
					out.println("<p>List of assigned papers for all conferences that you are a Reviewer:</p>");
					
					out.println("<table border=\"1\">");
					out.println("<tr><th>Paper ID</th><th>Title</th><th>Authors</th><th>Approve Status</th></tr>");
					while(rs_Papers.next()){
						int paper_id = rs_Papers.getInt(1);
						String paper_title = rs_Papers.getString(2);
						String authors = rs_Papers.getString(3);
						String approve_status="";
						String check_status = "SELECT C.process_over FROM Conference as C WHERE C.conf_id = (SELECT conf_id FROM paper_in_conf WHERE paper_id ="+paper_id+")";
						PreparedStatement CheckStatus_statement = con.prepareStatement(check_status);
						ResultSet  rs_check_status = CheckStatus_statement.executeQuery();
						double rating = 0;

						if(rs_check_status.next()){
							if((rs_check_status.getString(1)).equals("0")){
								approve_status = "In review";
								out.println("<tr><td>" + paper_id + "</td><td>"+ paper_title+"</td><td>"+authors+"</td><td>"+approve_status+"</td></tr>");
							}
							else{
								String cal_rating = "SELECT * FROM reviewed WHERE paper_id ="+Integer.toString(paper_id);
								PreparedStatement CalRating_statement = con.prepareStatement(cal_rating);
								ResultSet  rs_cal_rating = CalRating_statement.executeQuery();
								rating = 0.0;
								int i = 0;
								while(rs_cal_rating.next()){
									i++;
									rating+=rs_cal_rating.getInt(4);
								}
								if(i!=0)
									rating = rating/i;
								else
									rating = rating;
								if(rating > 4.0)
									approve_status = "Accept";
								else
									approve_status = "Reject";

								out.println("<tr><td>" + paper_id+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td><td>"+approve_status+"</td></tr>");

							}
						}
					}
				}
				else if(operationID==7){
					if(selected_paper==null){
						String get_Paperlist="SELECT paper_id, title, authors FROM Paper where paper_id in (SELECT paper_id FROM reviewed WHERE reviewer_id = "+reviewer_id+")";
						PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
						ResultSet  rs_Papers = PaperList_statement.executeQuery();
						out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
						out.println("<p>Papers assigned to you:</p>");
						out.println("<table border=\"1\">");
						out.println("<tr><th></th><th>Paper ID</th><th>Title</th><th>Authors</th></tr>");
						while(rs_Papers.next()){
							int paper_id = rs_Papers.getInt(1);
							String paper_title = rs_Papers.getString(2);
							String authors = rs_Papers.getString(3);

							out.println("<tr><td><a href=\"./AuthorReviewer?operationID=7&selected_paper="+paper_id+"\">View info.</a></td><td>"+paper_id+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td></tr>");
	//String approve_status="";
							String check_status = "SELECT C.process_over FROM Conference as C WHERE C.conf_id = (SELECT conf_id FROM paper_in_conf WHERE paper_id ="+paper_id+")";
							PreparedStatement CheckStatus_statement = con.prepareStatement(check_status);
							ResultSet  rs_check_status = CheckStatus_statement.executeQuery();
							double rating = 0;

							if(rs_check_status.next()){
								if((rs_check_status.getString(1)).equals("1")){
									out.println("<tr><td><a href=\"./AuthorReviewer?operationID=4&selected_paper="+paper_id+"\">Select</a></td><td>"+paper_id+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td></tr>");
								}
							}
						}
					}
					else{
						String get_title = "SELECT title FROM Paper WHERE paper_id="+selected_paper;
						PreparedStatement get_title_statement = con.prepareStatement(get_title);
						ResultSet  rs_get_title_status = get_title_statement.executeQuery();
						rs_get_title_status.next();
						String selected_title=rs_get_title_status.getString(1);
						out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
						out.println("<p>Final decision and reviews for <b>\""+selected_title+"\"</b></p>");
						out.println("<table border=\"1\">");
						out.println("<tr><th>Reviewer ID</th><th>Overall Rating</th><th>Comments</th></tr>");
						String cal_rating = "SELECT reviewer_id, overall_rating, comments FROM reviewed WHERE paper_id ="+selected_paper;
						PreparedStatement CalRating_statement = con.prepareStatement(cal_rating);
						ResultSet  rs_cal_rating = CalRating_statement.executeQuery();
						double rating = 0.0;
						String approve_status = "";
						int i = 0;
						while(rs_cal_rating.next()){
							i++;
							rating+=rs_cal_rating.getInt(2);
							int selected_reviewer_id = rs_cal_rating.getInt(1);
							int overall_rating = rs_cal_rating.getInt(2);
							String comments = rs_cal_rating.getString(3);
							out.println("<tr><td>" + Integer.toString(selected_reviewer_id)+ "</td><td>"+ Integer.toString(overall_rating)+"</td><td>"+comments+"</td></tr>");
						}
						if(i!=0)
							rating = rating/i;
						else
							rating = rating;
						if(rating > 4.0)
							approve_status = "Accept";
						else
							approve_status = "Reject";

						out.println("<p>Final decision of paper: <b>"+approve_status+"</b></p>");


					}
				}
				else if (operationID == 8){
					out.println("<p><a href=./AuthorReviewer?operationID=0>Go back to list of actions</a></p>");
					if(selected_paper==null){
						//String get_conferences = "SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP();";
						String get_Paperlist="SELECT DISTINCT P.paper_id, P.title, P.authors, P.abstract, C.conf_id FROM Paper as P, paper_in_conf as C WHERE P.paper_id IN (SELECT paper_id FROM reviewed WHERE reviewer_id = "+reviewer_id+") AND C.conf_id IN (SELECT conf_id FROM Conference as C WHERE submission_end_time > CURRENT_TIMESTAMP())";


						PreparedStatement PaperList_statement = con.prepareStatement(get_Paperlist);
						ResultSet  rs_Papers = PaperList_statement.executeQuery();
					//	out.println("<p><a href=\"./AuthorReviewer?operationID=2&selected_paper=new\">Add new paper</a></p>");
						out.println("<p>Papers that you may submit or edit a review:</p>");

						out.println("<table border=\"1\">");
						out.println("<tr><th></th><th>Paper ID</th><th>Title</th><th>Authors</th><th>Abstract</th><th>Conference ID</th></tr>");
						while(rs_Papers.next()){
							int paper_id = rs_Papers.getInt(1);
							String paper_title = rs_Papers.getString(2);
							String authors = rs_Papers.getString(3);
							String abstract_text = rs_Papers.getString(4);
							String conf_id = rs_Papers.getString(5);

							out.println("<tr><td><a href=\"./AuthorReviewer?operationID=8&selected_paper="+Integer.toString(paper_id)+"\">Select</a></td><td>" + Integer.toString(paper_id)+ "</td><td>"+ paper_title+"</td><td>"+authors + "</td><td>"+abstract_text+"</td><td>"+conf_id+"</td></tr>");

						}
					}
					else if(selected_paper.equals("update")){
						String updated_paper_id = request.getParameter("updated_paper");
						String rating_str = request.getParameter("rating_str");
						String comments_str = request.getParameter("comments_str");
				
						String update_reviewer = "UPDATE reviewed SET overall_rating="+rating_str+", comments='"+comments_str+"', approve_status=1 WHERE paper_id = "+updated_paper_id+" AND reviewer_id="+reviewer_id;
						PreparedStatement update_paper_stmt = con.prepareStatement(update_reviewer);
						update_paper_stmt.executeUpdate();
						out.println("<p>Review successfully updated!</p>");
					}
					else{
					//if the Author chose a paper that is before submission deadline
						String get_Paper="SELECT DISTINCT R.paper_id, R.overall_rating, R.comments FROM reviewed as R WHERE R.paper_id ="+selected_paper;
						PreparedStatement get_paper_stmt = con.prepareStatement(get_Paper);
						ResultSet rs_paper = get_paper_stmt.executeQuery();

						String check_self_edit = "SELECT * FROM authored WHERE author_id="+author_id+" AND paper_id ="+selected_paper;
						PreparedStatement check_self_stmt = con.prepareStatement(check_self_edit);
						ResultSet rs_check_self = check_self_stmt.executeQuery();
						if(rs_check_self.next()){
							out.println("<p>You cannot review your own paper!</p>");
						}
						else if(rs_paper.next()){
				
							String comments = rs_paper.getString(3);
							int overall_rating = rs_paper.getInt(2);
							int paper_id = rs_paper.getInt(1);

							out.println("<form method=post action=../servlet/AuthorReviewer>");
							out.println("<h3>Submit or Edit Review for Paper ID = <b> "+Integer.toString(paper_id)+"</b>:</h3>");
							out.println("<p>");
							out.println("<label for=\"overall_rating_str\">Overall Rating:</label><br>");
							if(overall_rating!=0)
								out.println("<textarea name=\"rating_str\" cols=\"10\" rows=\"1\" maxlength=10>"+Integer.toString(overall_rating)+"</textarea></br>");
							else
								out.println("<textarea name=\"rating_str\" cols=\"10\" rows=\"1\" maxlength=10></textarea></br>");
							out.println("</p>");
							out.println("<p>");
							out.println("<label for=\"comments_str\">Comments:</label></br>");
							if(comments!=null)
								out.println("<textarea name=\"comments_str\" cols=\"40\" rows=\"7\" maxlength=300>"+comments+"</textarea></br>");
							else
								out.println("<textarea name=\"comments_str\" cols=\"40\" rows=\"7\" maxlength=300></textarea></br>");
							out.println("<small>Character limit: 300</small>");
							out.println("</p>");
							
							out.println("<input type=\"hidden\" name=\"operationID\" value=\"8\">");
							out.println("<input type=\"hidden\" name=\"updated_paper\" value=\""+Integer.toString(paper_id)+"\">");
							out.println("<input type=\"hidden\" name=\"selected_paper\" value=\"update\">");
							out.println("<p> <input type=submit name=\"paper_submit\" value=\"Submit\"> </p></form>");
					//	out.println("<p><a href=\"AuthorReviewer?operationID=2\">Go back to list</a></p>");
						}


					}

					
				}
			}
			out.println("</body>");
			out.println("</html>");
			
			session.setAttribute("author_id", author_id);
			session.setAttribute("reviewer_id", reviewer_id);
			session.setAttribute("selected_role", selected_role);
			
			
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
