import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Chair extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
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
			Connection con2 = null;
			
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
			con2 = DriverManager.getConnection(url, userName, password);

			//
			// Get operationID
			//
			String s_operationID = request.getParameter("operationID");
			int operationID = Integer.parseInt(s_operationID);

			if (operationID == 0)
			{
				out.println("<html>");
				out.println("<body>");
				out.println("<p><a href=Login?logout=true>Logout</a><p>");

				out.println("<h1>Chair Menu</h1>");

				out.println("<h2>What would you like to do?</h2>");
				out.println("<p><a href=\"Chair?operationID=1\">Create a conference</a><p>");
				out.println("<p><a href=\"Chair?operationID=2\">Add a reviewer for a conference</a><p>");
				out.println("<p><a href=\"Chair?operationID=3\">Assign each paper in a conference to a reviewer</a><p>");
				out.println("<p><a href=\"Chair?operationID=4\">Compute the papers' decision for a conference</a><p>");
				out.println("<p><a href=\"Chair?operationID=5\">Modify conference information</a><p>");
				
				out.println("</body>");
				out.println("</html>");
			}
			if (operationID == 1)
			{
				Statement stmt = con.createStatement();
				ResultSet rs = null;

				out.println("<html>");
				out.println("<head>");
				String title = "Create Conference";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");

				out.println("<p><a href=Login?logout=true>Logout</a><p>");

				String create_submit = request.getParameter("create_submit");
				if (create_submit != null)
				{
					//
					// Error flags
					// 	
					boolean wrongType = false;
					boolean missingField = false;
					boolean timeStampError = false;
					boolean invalidEndTime = false;
					boolean insertError = false;
	
					//
					// Get parameters from the form
					//		
					String name = request.getParameter("name");
					if (name == "" || name == null)
					{ 
						missingField = true; 
					}
					
					String submit_start_time = request.getParameter("submission_start_time");
						java.util.Date util_start_time = null;
					java.sql.Timestamp sql_start_time = null;
					if (submit_start_time == "" || submit_start_time == null)
					{ 
						missingField = true; 
					}
					else
					{
						try
						{
							DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
							util_start_time = (java.util.Date)formatter.parse(submit_start_time);
							sql_start_time = new java.sql.Timestamp(util_start_time.getTime());
						}
						catch (Exception e)
						{
							timeStampError = true;
						}
					}
	
					String submit_end_time = request.getParameter("submission_end_time");
					java.util.Date util_end_time = null;
					java.sql.Timestamp sql_end_time = null;
					if (submit_end_time == "" || submit_end_time == null)
					{ 
						missingField = true; 
					}
					else
					{
						try
						{
							DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
							util_end_time = (java.util.Date)formatter.parse(submit_end_time);
							sql_end_time = new java.sql.Timestamp(util_end_time.getTime());
						}
						catch (Exception e)
						{
							timeStampError = true;
						}
					}
	
					String s_num_review_per_paper = request.getParameter("num_review_per_paper"); 
					int num_review_per_paper = 0;
					if (s_num_review_per_paper != "" && s_num_review_per_paper != null)
					{
						try 
						{ 
							num_review_per_paper = Integer.parseInt(s_num_review_per_paper); 
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
	
					String s_max_review_per_reviewer = request.getParameter("max_review_per_reviewer");
					int max_review_per_reviewer = 0;
					if (s_max_review_per_reviewer != "" && s_max_review_per_reviewer != null)
					{
						try 
						{ 	
							max_review_per_reviewer = Integer.parseInt(s_max_review_per_reviewer);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
				
					//
					// Check if subission end time is earlier than current time or the submission start time.
					//
					if (!missingField && !wrongType && !timeStampError)
					{
						DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
						java.util.Date current_time = new java.util.Date();
						//out.println("<p>Current Time: " + dateFormat.format(current_time) + "</p>");
						//out.println("<p>Start Time: " + dateFormat.format(util_start_time) + "</p>");
						//out.println("<p>End Time: " + dateFormat.format(util_end_time) + "</p>");
						if (util_end_time.before(util_start_time) || util_end_time.before(current_time))
						{
							invalidEndTime = true;
						}
					}
					
					//
					// Insert conference's query
					//
					if (!missingField && !wrongType && !timeStampError && !invalidEndTime)
					{
						String insertQuery = "INSERT INTO Conference (name, submission_start_time, submission_end_time, num_review_per_paper, max_review_per_reviewer, process_over) VALUES(?, ?, ?, ?, ?, ?)";
		
						try
						{
							con.setAutoCommit(false);
							PreparedStatement pstmt = con.prepareStatement(insertQuery);
							pstmt.setString(1, name);
							pstmt.setTimestamp(2, sql_start_time);
							pstmt.setTimestamp(3, sql_end_time);
							pstmt.setInt(4, num_review_per_paper);
							pstmt.setInt(5, max_review_per_reviewer);
							pstmt.setInt(6, 0);
							pstmt.executeUpdate();
							con.commit();
							con.setAutoCommit(true);
						}
						catch (SQLException ex)
						{
							System.err.println("SQLException:  " + ex.getMessage());
							con.rollback();
							con.setAutoCommit(true);
							insertError = true;	
						}
					}
		
					//
					// Reporting Errors	
					//
					if (missingField || wrongType || timeStampError || invalidEndTime || insertError)
					{
						out.println("<p>There are error(s) when creating conferece:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (timeStampError)
						{ out.println("<li>The time format is wrong. Please use this format: MM-dd-yyyy HH:mm.</li>"); }
						if (invalidEndTime)
						{ out.println("<li>The <i>Submission End Time</i> cannot be ealier than the current time or the <i>Submission Start Time</i>.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=1\">Try adding a conference again</a></p>");	
					}
					else
					{
						out.println("<p>Succesfully added conference information.<p>");
						out.println("<p><a href=\"Chair?operationID=1\">Add more conference</a></p>");
					}
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a></p>");
				}
				else
				{
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");
					out.println("<form method=get action=Chair>");
					out.println("<h3>Create Conference</h3>");
					out.println("<small> Every field is required </small>");
					out.println("<p>");
 					out.println("<label for=\"name\">Conference's Name:</label></br>");
 					out.println("<input type=\"text\" name=\"name\" maxlength=30>");
					out.println("</p>");
					out.println("<p>");
  					out.println("<label for=\"submission_start_time\">Submission Start Time:</label></br>");
					out.println("<input type=\"text\" name=\"submission_start_time\" maxlength=20>");
					out.println("<small> (MM-DD-YYYY hh:mm) </small>");
					out.println("</p>");
					out.println("<p>");
  					out.println("<label for=\"submission_end_time\">Submission End Time:</label></br>");
  					out.println("<input type=\"text\" name=\"submission_end_time\" maxlength=20>");
  					out.println("<small> (MM-DD-YYYY hh:mm) </small>");
					out.println("</p>");
					out.println("<p>");
					out.println("<label for=\"num_review_per_paper\">Number of reviews needed for each paper:</label></br>");
					out.println("<input type=\"text\" name=\"num_review_per_paper\" size=5 maxlength=5>");
					out.println("</p>");
					out.println("<p>");
					out.println("<label for=\"max_review_per_reviewer\">Maximum number of papers each reviewer can review:</label></br>");
					out.println("<input type=\"text\" name=\"max_review_per_reviewer\" size=5 maxlength=5>");
					out.println("</p>");
					out.println("<input type=\"hidden\" name=\"operationID\" value=\"1\">");
					out.println("<p> <input type=submit name=\"create_submit\" value=\"Submit\"> </p>");
					out.println("</form>");
				}
					out.println("</body>");
					out.println("</html>");
	
					if (rs != null)
					{ 
						rs.close(); 
					}
					stmt.close();
			}
			else if (operationID == 2)
			{
					
				Statement stmt = con.createStatement();
				ResultSet rs = null;

				out.println("<html>");
				out.println("<head>");
				String title = "Add Reviewer";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				 
				out.println("<p><a href=Login?logout=true>Logout</a><p>");
	
				String reviewer_submit = request.getParameter("reviewer_submit");
				if (reviewer_submit != null)
				{
					out.println("<small>Adding reviewer...</small>");

					boolean missingField = false;
					boolean wrongType = false;
					boolean confIdNotExist = false;
					boolean revIdNotExist = false;
					boolean dupRevId = false;
					boolean insertError = false;

					String s_conf_id = request.getParameter("conf_id");
					int conf_id = 0;
					if (s_conf_id != "" && s_conf_id != null)
					{
						try 
						{ 	
							conf_id = Integer.parseInt(s_conf_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
					
					String email = request.getParameter("email");
					if (email == "" || email == null)
					{ 
						missingField = true; 
					}
				
					int reviewer_id = 0;	
					if (!missingField && !wrongType)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT conf_id FROM Conference WHERE conf_id=(SELECT conf_id FROM Conference WHERE submission_end_time>CURRENT_TIMESTAMP AND process_over=0 AND conf_id=" + conf_id + ")");
						if (rs != null && !rs.isBeforeFirst())
						{
							confIdNotExist = true;
						}
						
						rs = null;
						rs = stmt.executeQuery("SELECT reviewer_id FROM Reviewer WHERE email='" + email + "'");
						if (rs != null && rs.next())
						{
							reviewer_id = rs.getInt("reviewer_id");
						}
						else
						{
							revIdNotExist = true;
						}
						
						if (!confIdNotExist && !revIdNotExist)
						{
							rs = null;
							rs = stmt.executeQuery("SELECT reviewer_id FROM reviewer_in_conf WHERE reviewer_id=" + reviewer_id + " AND conf_id=" + conf_id);
							if (rs != null && rs.isBeforeFirst())
							{
								dupRevId = true;
							}
						}
					}
					if (!missingField && !wrongType && !confIdNotExist && !dupRevId && !revIdNotExist)
					{
                                        	String insertQuery = "INSERT INTO reviewer_in_conf (conf_id, reviewer_id) VALUES(?, ?)";

                                       		try
                                   		{
							con.setAutoCommit(false);
							PreparedStatement pstmt = con.prepareStatement(insertQuery);
							pstmt.setInt(1, conf_id);
							pstmt.setInt(2, reviewer_id);
							pstmt.executeUpdate();
							con.commit();
							con.setAutoCommit(true);
						}
                                        	catch (SQLException ex)
                                        	{
							System.err.println("SQLException:  " + ex.getMessage());
							con.rollback();
							con.setAutoCommit(true);
							insertError = true;
                                        	}
					}
					//
					// Reporting Errors	
					//
					if (missingField || wrongType || confIdNotExist || revIdNotExist || dupRevId || insertError)
					{
						out.println("<p>There are error(s) when adding reviewer:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (confIdNotExist)
						{ out.println("<li>An invalid <i>Conference Number</i> was entered. Either because the number does not exist, or because the conference corresponding to the number is over.</li>"); }
						if (revIdNotExist)
						{ out.println("<li>There is no reviewer corresponding to the email that was entered.</li>"); }
						if (dupRevId)
						{ out.println("<li>The reviewer with this email is already in the conference.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=2\">Try adding a reviewer again</a></p>");	
					}
					else
					{
						out.println("<p>Succesfully added reviewer.<p>");
						out.println("<p><a href=\"Chair?operationID=2\">Add more reviewer</a></p>");
					}
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a></p>");
				}
				else
				{
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");	
					out.println("<form method=get action=Chair>");
					out.println("<h3>Add a reviewer for a conference</h3>");
					out.println("<small> Every field is required </small>");
					out.println("<p>");
  					out.println("<label for=\"conf_id\">Conference number:</label></br>");
  					out.println("<input type=\"text\" name=\"conf_id\" size=10 maxlength=10></br>");
 	 				out.println("<small> Look at the table below to decide the conference number. </small>");
					out.println("</p>");
					out.println("<p>");
					out.println("<label for=\"email\">Reviewer's email:</label></br>");
					out.println("<input type=\"text\" name=\"email\" size=30 maxlength=30></br>");
					out.println("<small> Example: example@domain.com </small>");
					out.println("</p>");	
					out.println("<input type=\"hidden\" name=\"operationID\" value=\"2\">");
					out.println("<p> <input type=submit name=\"reviewer_submit\" value=\"Submit\"> </p>");
					out.println("</form>");
					out.println("<h3>Here are the conferences that you can add reviewers to. Please select one:<h3>");
					rs = null;
					rs = stmt.executeQuery("SELECT * FROM Conference WHERE submission_end_time>CURRENT_TIMESTAMP AND process_over=0");
					out.println("<table border=\"1\">");
					out.println("<tr><th>Conference Number</th><th>Conference name</th><th>Submission start time</th><th>Submission end time</th><th>Number of reviews required per paper</th><th>Maximum reviews per reviewer</th></tr>");
					
					if (rs != null)
					{
						if (!rs.isBeforeFirst())
						{
							out.println("<p>There are currently no conference.</p>");
						}
						else
						{
							while (rs.next()) 
							{
                       			     	    		int conf_id = rs.getInt(1);
                       	       				  	String name = rs.getString(2);
								String submission_start_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(3));
                               				 	String submission_end_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(4));
                               				 	int num_review_per_paper = rs.getInt(5);
                                				int max_review_per_reviewer = rs.getInt(6);
		
		                               			out.println("<tr><td>" + conf_id
								+ "</td><td>" + name 
								+ "</td><td>" + submission_start_time  
								+ "</td><td>" + submission_end_time
								+ "</td><td>" + num_review_per_paper 
								+ "</td><td>" + max_review_per_reviewer 
               	 		        	                + "</td></tr>");
                       					}	
						}
					}
					
				}	
	
				out.println("</body>");
				out.println("</html>");
	
				if (rs != null)
				{ 
					rs.close(); 
				}
				stmt.close();
			}

			else if (operationID == 3)
			{	
				Statement stmt = con.createStatement();
				Statement stmt2 = con2.createStatement();
				ResultSet rs = null;
	
				out.println("<html>");
				out.println("<head>");
				String title = "Assign Papers";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				
				out.println("<p><a href=Login?logout=true>Logout</a><p>");

				String conf_submit = request.getParameter("conf_submit");
				String assign_submit = request.getParameter("assign_submit");

				if (assign_submit != null)
				{
					boolean missingField = false;
					boolean wrongType = false;
					boolean pIDNotExist = false;
					boolean rIDNotExist = false;
					boolean reviewExist = false;
					boolean reviewAuthor = false;
					boolean cannotReview = false;
					boolean insertError = false;

					String s_conf_id = request.getParameter("conf_id");
					int conf_id = 0;
					if (s_conf_id != "" && s_conf_id != null)
					{
						try 
						{ 	
							conf_id = Integer.parseInt(s_conf_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
					
					String s_paper_id = request.getParameter("paper_id");
					int paper_id = 0;
					if (s_paper_id != "" && s_paper_id != null)
					{
						try 
						{ 	
							paper_id = Integer.parseInt(s_paper_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
				
					String s_reviewer_id = request.getParameter("reviewer_id");
					int reviewer_id = 0;
					if (s_reviewer_id != "" && s_reviewer_id != null)
					{
						try 
						{ 	
							reviewer_id = Integer.parseInt(s_reviewer_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}

					if (!missingField && !wrongType)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT DISTINCT reviewer_id FROM reviewer_in_conf WHERE reviewer_id=" + reviewer_id + " AND conf_id=" + conf_id);
						if (rs != null && !rs.isBeforeFirst())
						{
							rIDNotExist = true;
						}
						rs = null;
						rs = stmt.executeQuery("SELECT DISTINCT paper_id FROM paper_in_conf  WHERE paper_id=" + paper_id + " AND conf_id=" + conf_id);
						if (rs != null && !rs.isBeforeFirst())
						{
							pIDNotExist = true;
						}
					}
			
					if (!missingField && !wrongType && !rIDNotExist && !pIDNotExist)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT * FROM reviewer_in_conf NATURAL JOIN paper_in_conf NATURAL JOIN reviewed WHERE reviewer_id=" + reviewer_id + " AND paper_id=" + paper_id + " AND conf_id=" + conf_id);
						if (rs != null && rs.isBeforeFirst())
						{
							reviewExist = true;
						}
						
						if (!reviewExist)
						{
							rs = null;
							rs = stmt.executeQuery("SELECT * FROM authored NATURAL JOIN Paper Natural JOIN paper_in_conf WHERE paper_id=" + paper_id + " AND author_id=" + reviewer_id + " AND conf_id=" + conf_id);
							if (rs != null && rs.isBeforeFirst())
							{
								reviewAuthor = true;
							}
						}
					}

					if (!missingField && !wrongType && !rIDNotExist && !pIDNotExist && !reviewExist && !reviewAuthor)
					{
						rs = null;
						int max_review_per_reviewer = 0;
						rs = stmt.executeQuery("SELECT max_review_per_reviewer FROM Conference WHERE conf_id=" + conf_id);
						if (rs.next())
						{
							max_review_per_reviewer = rs.getInt("max_review_per_reviewer");
						}
						
						rs = null;
						int num_review_left = max_review_per_reviewer;
						rs = stmt2.executeQuery("SELECT COUNT(*) FROM reviewer_in_conf NATURAL JOIN reviewed WHERE reviewer_id=" + reviewer_id + " AND conf_id=" + conf_id);
						if (rs.next())
						{
							num_review_left = max_review_per_reviewer - rs.getInt(1);
						}
						if (num_review_left <= 0)
						{
							cannotReview = true;
						}
					}

					if (!missingField && !wrongType && !rIDNotExist && !pIDNotExist && !reviewExist && !reviewAuthor && !cannotReview)
					{
						String insertQuery = "INSERT INTO reviewed (conf_id, reviewer_id, paper_id, overall_rating, comments, approve_status) VALUES(?, ?, ?, ?, ?, ?)";

                                       		try
                                   		{
							con.setAutoCommit(false);
							PreparedStatement pstmt = con.prepareStatement(insertQuery);
							pstmt.setInt(1, conf_id);
							pstmt.setInt(2, reviewer_id);
							pstmt.setInt(3, paper_id);
							pstmt.setInt(4, 0);
							pstmt.setString(5, null);
							pstmt.setInt(6, 0);
							pstmt.executeUpdate();
							con.commit();
							con.setAutoCommit(true);
						}
                                        	catch (SQLException ex)
                                        	{
							System.err.println("SQLException:  " + ex.getMessage());
							con.rollback();
							con.setAutoCommit(true);
							insertError = true;
                                        	}
					}
					//
					// Reporting Errors	
					//
					if (missingField || wrongType || rIDNotExist || pIDNotExist || reviewExist || reviewAuthor || cannotReview || insertError)
					{
						out.println("<p>There are error(s) when assigning paper:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (pIDNotExist)
						{ out.println("<li>An invalid <i>Paper ID</i> was entered. Either because the paper does not exist, or because the paper is not in this conference.</li>"); }
						if (rIDNotExist)
						{ out.println("<li>An invalid <i>Reviewer ID</i> was entered. Either because the reviewer does not exist, or because the reviewer is not in this conference.</li>"); }
						if (reviewExist)
						{ out.println("<li>The reviewer is already assigned to this paper.</li>"); }
						if (reviewAuthor)
						{ out.println("<li>Cannot assign a reviewer to a paper to which him/her is the author.</li>"); }
						if (cannotReview)
						{ out.println("<li>Cannot assign to this reviewer because he/she has reached the maximum amount of papers he/she is allowed to have.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=3\">Try assigning the paper again</a></p>");	
					}
					else
					{
						out.println("<p>Succesfully assigned a paper to a reviewer.<p>");
						out.println("<p><a href=\"Chair?operationID=3\">Assign more paper</a></p>");
					}
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a></p>");
				}
				else if (conf_submit != null)
				{
					boolean missingField = false;
					boolean wrongType = false;
					boolean confIdNotExist = false;
					boolean insertError = false;

					String s_conf_id = request.getParameter("conf_id");
					int conf_id = 0;
					if (s_conf_id != "" && s_conf_id != null)
					{
						try 
						{ 	
							conf_id = Integer.parseInt(s_conf_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}

					if (!missingField && !wrongType)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT conf_id FROM Conference WHERE conf_id=(SELECT conf_id FROM Conference WHERE submission_end_time>CURRENT_TIMESTAMP AND process_over=0 AND conf_id=" + conf_id + ")");
						if (rs != null && !rs.isBeforeFirst())
						{
							confIdNotExist = true;
						}	
					}
					
					if (missingField || wrongType || confIdNotExist)
					{
						out.println("<p>There are error(s) when finding conference:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (confIdNotExist)
						{ out.println("<li>An invalid <i>Conference Number</i> was entered. Either because the number does not exist, or because the conference corresponding to the number is over.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=3\">Try again</a></p>");
					}
					else
					{
						out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");
						out.println("<form method=get action=Chair>");
						out.println("<h3>Assign a Paper to a reviewer</h3>");
						out.println("<small> Every field is required </small>");
						out.println("<p>");
  						out.println("<label for=\"paper_id\">Paper ID:</label></br>");
  						out.println("<input type=\"text\" name=\"paper_id\" size=10 maxlength=10></br>");
 	 					out.println("<small> Look at the tables below to decide the Paper ID. </small>");
						out.println("</p>");
						out.println("<p>");
						out.println("<label for=\"reviewer_id\">Reviewer ID:</label></br>");
						out.println("<input type=\"text\" name=\"reviewer_id\" size=10 maxlength=10></br>");
						out.println("<small> Look at the tables below to decide the Reviewer ID. </small>");
						out.println("</p>");	
						out.println("<input type=\"hidden\" name=\"operationID\" value=\"3\">");
						out.println("<input type=\"hidden\" name=\"conf_id\" value=\"" + conf_id + "\">"); 
						out.println("<p> <input type=submit name=\"assign_submit\" value=\"Submit\"> </p>");
						out.println("</form>");

						out.println("<h3>Please use these tables for reference.</h3>");
						rs = null;
						int max_review_per_reviewer = 0;
						rs = stmt.executeQuery("SELECT max_review_per_reviewer FROM Conference WHERE conf_id=" + conf_id);
						if (rs.next())
						{
							max_review_per_reviewer = rs.getInt("max_review_per_reviewer");
						}
						rs = null;
						rs = stmt.executeQuery("SELECT reviewer_id, first_name, last_name FROM Reviewer NATURAL JOIN reviewer_in_conf WHERE conf_id=" + conf_id);
										
						if (rs != null)
						{	
							if (!rs.isBeforeFirst())
							{
								out.println("<table border=\"0\">");
								out.println("<caption style=\"text-align:left\"></br><b>This table shows the current reviewers in this conference:</b>");
								out.println("<p>There are currently no reviewers in this conference.</p></caption>");
							}
							else
							{
								out.println("<table border=\"1\">");
								out.println("<caption style=\"text-align:left\"><b>This table shows the current reviewers in this conference:</b></caption>");
								out.println("<tr><th>Reviewer ID</th><th>First Name</th><th>Last Name</th><th>Number of Reviews Left</th></tr>");
								while (rs.next()) 
								{
                       				     	    		int reviewer_id = rs.getInt(1);
                       	       					  	String first_name = rs.getString(2);
									String last_name = rs.getString(3);
								
									ResultSet rs_1 = null;
									rs_1 = stmt2.executeQuery("SELECT COUNT(*) FROM reviewer_in_conf NATURAL JOIN reviewed WHERE reviewer_id=" + reviewer_id + " AND conf_id=" + conf_id);
									int num_review_left = max_review_per_reviewer;
									if (rs_1.next())
									{
										num_review_left = max_review_per_reviewer - rs_1.getInt(1);
									}
									out.println("<tr><td>" + reviewer_id
									+ "</td><td>" + first_name 
									+ "</td><td>" + last_name
									+ "</td><td>" + num_review_left
               	 		        	        	        + "</td></tr>");
                       						}		
							}	
						}	
						rs = null;
						rs = stmt.executeQuery("SELECT num_review_per_paper FROM Conference WHERE conf_id=" + conf_id);
						int num_review_per_paper = 0;
						if (rs.next())
						{
							num_review_per_paper = rs.getInt("num_review_per_paper");
						}
						rs = null;
						rs = stmt.executeQuery("SELECT paper_id, title, authors FROM Paper NATURAL JOIN paper_in_conf WHERE conf_id=" + conf_id);
										
						if (rs != null)
						{
							
							if (!rs.isBeforeFirst())
							{
								out.println("<table border=\"0\">");
								out.println("<caption style=\"text-align:left\"></br><b>This table shows the current papers in this conference:</b>");
								out.println("<p>There are currently no papers in this conference.</p></caption>");
							}
							else
							{
								out.println("<table border=\"1\">");
								out.println("<caption style=\"text-align:left\"></br><b>This table shows the current papers in this conference:</b></caption>");
								out.println("<tr><th>Paper ID</th><th>Title</th><th>Authors</th><th>Number of Reviews still needed</th></tr>");
								while (rs.next()) 
								{
                       				     	    		int paper_id = rs.getInt(1);
                       	       					  	String paperTitle = rs.getString(2);
									String authors = rs.getString(3);
								
									ResultSet rs_1 = null;
									rs_1 = stmt2.executeQuery("SELECT COUNT(*) FROM paper_in_conf NATURAL JOIN reviewed WHERE paper_id=" + paper_id + " AND conf_id=" + conf_id);
									int num_review_still_needed = num_review_per_paper;
									if (rs_1.next())
									{
										num_review_still_needed = num_review_per_paper - rs_1.getInt(1);
										if (num_review_still_needed <= 0)
										{
											num_review_still_needed = 0;
										}
									}
									out.println("<tr><td>" + paper_id
									+ "</td><td>" + paperTitle
									+ "</td><td>" + authors
									+ "</td><td>" + num_review_still_needed
               	 		        	        	        + "</td></tr>");
                       						}		
							}	
						}
						rs = null;
						rs = stmt.executeQuery("SELECT paper_id, title, authors, reviewer_id, first_name, last_name FROM (paper_in_conf NATURAL JOIN Paper) NATURAL JOIN (reviewer_in_conf NATURAL JOIN Reviewer) NATURAL JOIN reviewed WHERE conf_id=" + conf_id);
										
						if (rs != null)
						{
							
							if (!rs.isBeforeFirst())
							{
								out.println("<table border=\"0\">");
								out.println("<caption style=\"text-align:left\"></br><b>This table shows the current paper-reviewer pair assignment in this conference:</b>");
								out.println("<p>There are currently no assignments in this conference.</p></caption>");
							}
							else
							{
								out.println("<table border=\"1\">");
								out.println("<caption style=\"text-align:left\"></br><b>This table shows the current paper-reviewer pair assignment in this conference:</b></caption>");
								out.println("<tr><th>Paper ID</th><th>Title</th><th>Authors</th><th>Reviewer ID</th><th>First Name</th><th>Last Name</th></tr>");
								while (rs.next()) 
								{
                       				     	    		int paper_id = rs.getInt(1);
                       	       					  	String paperTitle = rs.getString(2);
									String authors = rs.getString(3);
									int reviewer_id = rs.getInt(4);
									String first_name = rs.getString(5);
									String last_name = rs.getString(6);
								
									out.println("<tr><td>" + paper_id
									+ "</td><td>" + paperTitle
									+ "</td><td>" + authors
									+ "</td><td>" + reviewer_id
									+ "</td><td>" + first_name
									+ "</td><td>" + last_name
               	 		        	        	        + "</td></tr>");
                       						}		
							}	
						}
					}
				}
				else
				{
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");	
					out.println("<form method=get action=Chair>");
					out.println("<h3>Assign each Paper in a Conference to Reviewers</h3>");
					out.println("<p>");
					out.println("<label for=\"conf_id\">Conference number:</label></br>");
					out.println("<input type=\"text\" name=\"conf_id\" size=10 maxlength=10></br>");
					out.println("<small> Look at the table below to decide the conference number. </small>");
					out.println("</p>");
					out.println("<input type=hidden name=operationID value=3>");
					out.println("<p> <input type=submit name=conf_submit value=\"Submit\"> </p>");
					out.println("</form>");
					out.println("<h3>Here are the conferences that you can assign papers to reviewers. Please select one:<h3>");
					rs = null;
					rs = stmt.executeQuery("SELECT * FROM Conference WHERE submission_end_time>CURRENT_TIMESTAMP and process_over=0");
					out.println("<table border=\"1\">");
					out.println("<tr><th>Conference Number</th><th>Conference name</th><th>Submission start time</th><th>Submission end time</th><th>Number of reviews required per paper</th><th>Maximum reviews per reviewer</th></tr>");
					
					if (rs != null)
					{
						if (!rs.isBeforeFirst())
						{
							out.println("<p>There are currently no conference.</p>");
						}
						else
						{
							while (rs.next()) 
							{
                       			     	    		int conf_id = rs.getInt(1);
                       	       				  	String name = rs.getString(2);
								String submission_start_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(3));
                               				 	String submission_end_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(4));
                               				 	int num_review_per_paper = rs.getInt(5);
                                				int max_review_per_reviewer = rs.getInt(6);
		
		                               			out.println("<tr><td>" + conf_id
								+ "</td><td>" + name 
								+ "</td><td>" + submission_start_time  
								+ "</td><td>" + submission_end_time
								+ "</td><td>" + num_review_per_paper 
								+ "</td><td>" + max_review_per_reviewer 
               	 		        	                + "</td></tr>");
                       					}	
						}
					}
				}
				out.println("</body>");
				out.println("</html>");
	
				if (rs != null)
				{ rs.close(); }
				stmt.close();
			}

			else if (operationID == 4)
			{
				Statement stmt = con.createStatement();
				Statement stmt2 = con2.createStatement();
				ResultSet rs = null;

				out.println("<html>");
				out.println("<head>");
				String title = "Trigger Computations";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				
				out.println("<p><a href=Login?logout=true>Logout</a><p>");

				String compute_submit = request.getParameter("compute_submit");
				if (compute_submit != null)
				{
					//
					// Error flags
					// 	
					boolean wrongType = false;
					boolean missingField = false;
					boolean invalidID = false;
					boolean noPaper = false;
					boolean noReviewer = false;
					boolean reviewNeeded = false;
					boolean needToReview = false;
					boolean insertError = false;

					//
					// Get parameters from the form
					//
					String s_conf_id = request.getParameter("conf_id");
					int conf_id = 0;
					if (s_conf_id != "" && s_conf_id != null)
					{
						try 
						{ 	
							conf_id = Integer.parseInt(s_conf_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
			
					if (!missingField && !wrongType)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT * FROM Conference WHERE submission_end_time<CURRENT_TIMESTAMP AND process_over=0 AND conf_id=" + conf_id);
						if (rs != null && !rs.isBeforeFirst())
						{
							invalidID = true;
						}
					}

					if (!missingField && !wrongType && !invalidID)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT num_review_per_paper FROM Conference WHERE conf_id=" + conf_id);
						int num_review_per_paper = 0;
						if (rs.next())
						{
							num_review_per_paper = rs.getInt("num_review_per_paper");
						}

						rs = null;
						rs = stmt.executeQuery("SELECT paper_id FROM paper_in_conf WHERE conf_id=" + conf_id);
						
						if (rs != null)
						{
							if (!rs.isBeforeFirst())
							{
								noPaper = true;
							}
							else
							{
								while (rs.next()) 
								{
                       							int paper_id = rs.getInt(1);
											
									ResultSet rs_1 = null;
									rs_1 = stmt2.executeQuery("SELECT COUNT(*) FROM paper_in_conf NATURAL JOIN reviewed WHERE paper_id=" + paper_id + " AND conf_id=" + conf_id);
									int num_review_still_needed = num_review_per_paper;
									if (rs_1.next())
									{
										num_review_still_needed = num_review_per_paper - rs_1.getInt(1);
										if (num_review_still_needed > 0)
										{
											reviewNeeded = true;
										}
									}
                       						}
							}
						}
						
						rs = null;	
						rs = stmt.executeQuery("SELECT * FROM reviewed WHERE conf_id=" + conf_id + " AND approve_status=0");
	
						if (rs != null && rs.isBeforeFirst())
						{
							needToReview = true;	
						}
					}
					
					if (!missingField && !wrongType && !invalidID && !noPaper && !noReviewer && !reviewNeeded && !needToReview)
					{
						String updateQuery = "UPDATE Conference SET process_over=? WHERE conf_id=" + conf_id;
		
						try
						{
							con.setAutoCommit(false);
							PreparedStatement pstmt = con.prepareStatement(updateQuery);
							pstmt.setInt(1, 1);
							pstmt.executeUpdate();
							con.commit();
							con.setAutoCommit(true);
						}
						catch (SQLException ex)
						{
							System.err.println("SQLException:  " + ex.getMessage());
							con.rollback();
							con.setAutoCommit(true);
							insertError = true;	
						}		
					}
					
					//
					// Reporting Errors	
					//
					if (missingField || wrongType || invalidID || noPaper || noReviewer || reviewNeeded || needToReview || insertError)
					{
						out.println("<p>There are error(s) when computing papers' decision:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (invalidID)
						{ out.println("<li>An invalid <i>Conference Number</i> was entered. Either because the number does not exist, or because the conference is not over yet.</li>"); }
						if (noPaper)
						{ out.println("<li>Cannot compute because there are no paper(s) in this conference.</li>"); }
						if (noReviewer)
						{ out.println("<li>Cannot compute because there are no reviewer(s) in this conference.</li>"); }
						if (reviewNeeded)
						{ out.println("<li>There are still paper(s) that haven't got enough reviews.</li>"); }
						if (needToReview)
						{ out.println("<li>There are still reviewer(s) that haven't submitted their reviews.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=4\">Try again</a></p>");
					}
					else
					{
						out.println("<p>Succesfully computed the papers' decision<p>");
						out.println("<p><a href=\"Chair?operationID=4\">Compute more decisions</a></p>");
					}
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a></p>");
				}
				else
				{
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");	
					out.println("<form method=get action=Chair>");
					out.println("<h3>Compute scores for papers in a conference.</h3>");
					out.println("<small> Every field is required </small>");
					out.println("<p>");
  					out.println("<label for=\"conf_id\">Conference number:</label></br>");
  					out.println("<input type=\"text\" name=\"conf_id\" size=10 maxlength=10></br>");
					out.println("</p>");
					out.println("<input type=\"hidden\" name=\"operationID\" value=\"4\">");
					out.println("<p> <input type=submit name=\"compute_submit\" value=\"Submit\"> </p>");
					out.println("</form>");
				}
				
				out.println("</body>");
				out.println("</html>");
	
				if (rs != null)
				{ rs.close(); }
				stmt.close();
			}

			else if (operationID == 5)
			{
				
				Statement stmt = con.createStatement();
				ResultSet rs = null;

				out.println("<html>");
				out.println("<head>");
				String title = "Modify Conference";
				out.println("<title>" + title + "</title>");
				out.println("</head>");
				out.println("<body bgcolor=white>");
				
				out.println("<p><a href=Login?logout=true>Logout</a><p>");

				String modify_submit = request.getParameter("modify_submit");
				String modifying_submit = request.getParameter("modifying_submit");
				
				if (modifying_submit != null)
				{
					//
					// Error flags
					// 	
					boolean wrongType = false;
					boolean missingField = false;
					boolean timeStampError = false;
					boolean invalidEndTime = false;
					boolean insertError = false;

					//
					// Get parameters from the form
					//

					String s_conf_id = request.getParameter("conf_id");
					int conf_id = 0;
					if (s_conf_id != "" && s_conf_id != null)
					{
						try 
						{ 	
							conf_id = Integer.parseInt(s_conf_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}	
	
					String name = request.getParameter("name");
					if (name == "" || name == null)
					{ 
						missingField = true; 
					}
					
					String submit_start_time = request.getParameter("submission_start_time");
					java.util.Date util_start_time = null;
					java.sql.Timestamp sql_start_time = null;
					if (submit_start_time == "" || submit_start_time == null)
					{ 
						missingField = true; 
					}
					else
					{
						try
						{
							DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
							util_start_time = (java.util.Date)formatter.parse(submit_start_time);
							sql_start_time = new java.sql.Timestamp(util_start_time.getTime());
						}
						catch (Exception e)
						{
							timeStampError = true;
						}
					}
	
					String submit_end_time = request.getParameter("submission_end_time");
					java.util.Date util_end_time = null;
					java.sql.Timestamp sql_end_time = null;
					if (submit_end_time == "" || submit_end_time == null)
					{ 
						missingField = true; 
					}
					else
					{
						try
						{
							DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm");
							util_end_time = (java.util.Date)formatter.parse(submit_end_time);
							sql_end_time = new java.sql.Timestamp(util_end_time.getTime());
						}
						catch (Exception e)
						{
							timeStampError = true;
						}
					}
	
					String s_num_review_per_paper = request.getParameter("num_review_per_paper"); 
					int num_review_per_paper = 0;
					if (s_num_review_per_paper != "" && s_num_review_per_paper != null)
					{
						try 
						{ 
							num_review_per_paper = Integer.parseInt(s_num_review_per_paper); 
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
	
					String s_max_review_per_reviewer = request.getParameter("max_review_per_reviewer");
					int max_review_per_reviewer = 0;
					if (s_max_review_per_reviewer != "" && s_max_review_per_reviewer != null)
					{
						try 
						{ 	
							max_review_per_reviewer = Integer.parseInt(s_max_review_per_reviewer);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}
					
					//
					// Check if subission end time is earlier than current time or the submission start time.
					//
					if (!missingField && !wrongType && !timeStampError)
					{
						DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
						java.util.Date current_time = new java.util.Date();
						if (util_end_time.before(util_start_time) || util_end_time.before(current_time))
						{
							invalidEndTime = true;
						}
					}
					
					if (!missingField && !wrongType && !timeStampError && !invalidEndTime)
					{
						String updateQuery = "UPDATE Conference SET name=?, submission_start_time=?, submission_end_time=?, num_review_per_paper=?, max_review_per_reviewer=? WHERE conf_id=" + conf_id;
		
						try
						{
							con.setAutoCommit(false);
							PreparedStatement pstmt = con.prepareStatement(updateQuery);
							pstmt.setString(1, name);
							pstmt.setTimestamp(2, sql_start_time);
							pstmt.setTimestamp(3, sql_end_time);
							pstmt.setInt(4, num_review_per_paper);
							pstmt.setInt(5, max_review_per_reviewer);
							pstmt.executeUpdate();
							con.commit();
							con.setAutoCommit(true);
						}
						catch (SQLException ex)
						{
							System.err.println("SQLException:  " + ex.getMessage());
							con.rollback();
							con.setAutoCommit(true);
							insertError = true;	
						}
					}
	
					//
					// Reporting Errors	
					//
					if (missingField || wrongType || timeStampError || invalidEndTime || insertError)
					{
						out.println("<p>There are error(s) when creating conferece:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (timeStampError)
						{ out.println("<li>The time format is wrong. Please use this format: MM-dd-yyyy HH:mm.</li>"); }
						if (invalidEndTime)
						{ out.println("<li>The <i>Submission End Time</i> cannot be ealier than the current time or the <i>Submission Start Time</i>.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=5\">Try again</a></p>");
					}
					else
					{
						out.println("<p>Succesfully modified conference information.<p>");
						out.println("<p><a href=\"Chair?operationID=5\">Modify Conference Again</a></p>");
					}
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a></p>");
				}	
				else if (modify_submit != null)
				{
					boolean missingField = false;
					boolean wrongType = false;
					boolean confIdNotExist = false;
					boolean insertError = false;

					String s_conf_id = request.getParameter("conf_id");
					int conf_id = 0;
					if (s_conf_id != "" && s_conf_id != null)
					{
						try 
						{ 	
							conf_id = Integer.parseInt(s_conf_id);
						}
						catch (Exception e)
						{ 
							wrongType = true; 
						}
					}
					else
					{
						missingField = true;
					}

					if (!missingField && !wrongType)
					{
						rs = null;
						rs = stmt.executeQuery("SELECT conf_id FROM Conference WHERE conf_id=(SELECT conf_id FROM Conference WHERE submission_end_time>CURRENT_TIMESTAMP AND process_over=0 AND conf_id=" + conf_id + ")");
						if (rs != null && !rs.isBeforeFirst())
						{
							confIdNotExist = true;
						}	
					}
						
					if (missingField || wrongType || confIdNotExist)
					{
						out.println("<p>There are error(s) when finding conference:</p>");
						out.println("<ul>");
						if (missingField)
						{ out.println("<li>Some required fields(s) are missing.</li>"); }
						if (wrongType)
						{ out.println("<li>Please enter integers for integer-valued fields.</li>"); }
						if (confIdNotExist)
						{ out.println("<li>An invalid <i>Conference Number</i> was entered. Either because the number does not exist, or because the conference corresponding to the number is over.</li>"); }
						if (insertError)
						{ out.println("<li>There was a problem inserting information. Please contact the web administrator.</li>"); }
						out.println("</ul>");
						out.println("<p>Please try again or return to the chair menu.</p>");
						out.println("<p><a href=\"Chair?operationID=5\">Try again</a></p>");
						out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");	
					}
					else
					{
						out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");
						out.println("<form method=get action=Chair>");
						out.println("<h3>Modify Conference</h3>");
						out.println("<small> Every field is required </small>");
						out.println("<p>");
 						out.println("<label for=\"name\">Conference's Name:</label></br>");
 						out.println("<input type=\"text\" name=\"name\" maxlength=30>");
						out.println("</p>");
						out.println("<p>");
  						out.println("<label for=\"submission_start_time\">Submission Start Time:</label></br>");
						out.println("<input type=\"text\" name=\"submission_start_time\" maxlength=20>");
						out.println("<small> (MM-DD-YYYY hh:mm) </small>");
						out.println("</p>");
						out.println("<p>");
  						out.println("<label for=\"submission_end_time\">Submission End Time:</label></br>");
  						out.println("<input type=\"text\" name=\"submission_end_time\" maxlength=20>");
  						out.println("<small> (MM-DD-YYYY hh:mm) </small>");
						out.println("</p>");
						out.println("<p>");
						out.println("<label for=\"num_review_per_paper\">Number of reviews needed for each paper:</label></br>");
						out.println("<input type=\"text\" name=\"num_review_per_paper\" size=5 maxlength=5>");
						out.println("</p>");
						out.println("<p>");
						out.println("<label for=\"max_review_per_reviewer\">Maximum number of papers each reviewer can review:</label></br>");
						out.println("<input type=\"text\" name=\"max_review_per_reviewer\" size=5 maxlength=5>");
						out.println("</p>");
						out.println("<input type=\"hidden\" name=\"operationID\" value=\"5\">");
						out.println("<input type=\"hidden\" name=\"conf_id\" value=\"" + conf_id + "\">");
						out.println("<p> <input type=submit name=\"modifying_submit\" value=\"Submit\"> </p>");
						out.println("</form>");
						out.println("<h3>These are the information currently stored in this conference.</h3>");
						rs = null;
						rs = stmt.executeQuery("SELECT * FROM Conference WHERE conf_id=" + conf_id);
						out.println("<table border=\"1\">");
						out.println("<tr><th>Conference name</th><th>Submission start time</th><th>Submission end time</th><th>Number of reviews required per paper</th><th>Maximum reviews per reviewer</th></tr>");
						if (rs.next()) 
						{
                       	       				String name = rs.getString(2);
							String submission_start_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(3));
                               				String submission_end_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(4));
                               				int num_review_per_paper = rs.getInt(5);
                                			int max_review_per_reviewer = rs.getInt(6);

							out.println("<tr><td>" + name 
							+ "</td><td>" + submission_start_time  
							+ "</td><td>" + submission_end_time
							+ "</td><td>" + num_review_per_paper 
							+ "</td><td>" + max_review_per_reviewer 
               	 		        	        + "</td></tr>");
                       				}
					}
				}
				else
				{
					out.println("<p><a href=\"Chair?operationID=0\">Go back to chair menu</a><p>");	
					out.println("<form method=get action=Chair>");
					out.println("<h3>Modify a conference</h3>");
					out.println("<small> Every field is required </small>");
					out.println("<p>");
  					out.println("<label for=\"conf_id\">Conference number:</label></br>");
  					out.println("<input type=\"text\" name=\"conf_id\" size=10 maxlength=10></br>");
 	 				out.println("<small> Look at the table below to decide the conference number. </small>");
					out.println("</p>");
					out.println("<input type=\"hidden\" name=\"operationID\" value=\"5\">");
					out.println("<p> <input type=submit name=\"modify_submit\" value=\"Submit\"> </p>");
					out.println("</form>");
					out.println("<h3>Here are the conferences that you can modify. Please select one:<h3>");
					rs = null;
					rs = stmt.executeQuery("SELECT * FROM Conference WHERE submission_end_time>CURRENT_TIMESTAMP and process_over=0");
					out.println("<table border=\"1\">");
					out.println("<tr><th>Conference Number</th><th>Conference name</th><th>Submission start time</th><th>Submission end time</th><th>Number of reviews required per paper</th><th>Maximum reviews per reviewer</th></tr>");
					
					if (rs != null)
					{
						if (!rs.isBeforeFirst())
						{
							out.println("<p>There are currently no conference.</p>");
						}
						else
						{
							while (rs.next()) 
							{
                       			     	    		int conf_id = rs.getInt(1);
                       	       				  	String name = rs.getString(2);
								String submission_start_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(3));
                               				 	String submission_end_time = new SimpleDateFormat("MM-dd-yyyy HH:mm").format(rs.getTimestamp(4));
                               				 	int num_review_per_paper = rs.getInt(5);
                                				int max_review_per_reviewer = rs.getInt(6);
		
		                               			out.println("<tr><td>" + conf_id
								+ "</td><td>" + name 
								+ "</td><td>" + submission_start_time  
								+ "</td><td>" + submission_end_time
								+ "</td><td>" + num_review_per_paper 
								+ "</td><td>" + max_review_per_reviewer 
               	 		        	                + "</td></tr>");
                       					}	
						}
					}	
				}
				out.println("</body>");
				out.println("</html>");

				if (rs != null)
				{ 
					rs.close(); 
				}
				stmt.close();	
			}
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
}
