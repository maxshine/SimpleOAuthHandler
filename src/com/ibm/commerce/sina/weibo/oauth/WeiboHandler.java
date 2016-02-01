package com.ibm.commerce.sina.weibo.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Formatter;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class WeiboHandler
 */
@WebServlet("/WeiboHandler")
public class WeiboHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String TOKEN_BASE_URL = "";
	private static String CLIENT_ID = "";
	private static String CLIENT_SECRETE = "";
	private static String TOKEN_CALLBACK_BASE_URL = "";
	private static boolean DEBUG = false;
	private static String SERVLET_NAME="WeiboHandler";
	private static String PROPERTIES_FILE = "weibo.properties";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WeiboHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
        ServletContext ctx = this.getServletContext();
        InputStream is = ctx.getResourceAsStream("/WEB-INF/"+PROPERTIES_FILE);
        Properties prop = new Properties();
        try {
			prop.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        TOKEN_BASE_URL = prop.getProperty("TOKEN_BASE_URL", "");
        CLIENT_ID = prop.getProperty("CLIENT_ID", "");
        CLIENT_SECRETE = prop.getProperty("CLIENT_SECRETE", "");
        TOKEN_CALLBACK_BASE_URL = prop.getProperty("TOKEN_CALLBACK_BASE_URL", "");
        DEBUG = Boolean.valueOf(prop.getProperty("DEBUG"));
        
        if (DEBUG == true) {
        	Formatter fmt = new Formatter();
        	fmt.format("TOKEN_BASE_URL=%s,\n CLIENT_ID=%s,\n CLIENT_SECRETE=%s,\n TOKEN_CALLBACK_URL=%s\n"
        			, TOKEN_BASE_URL, CLIENT_ID, CLIENT_SECRETE, TOKEN_CALLBACK_BASE_URL);
        	System.out.print(SERVLET_NAME + " servlet is initialized!\n");
        	System.out.print(SERVLET_NAME + " Loaded properties are : \n");
        	System.out.println(fmt.toString());
        	fmt.close();
        }
        
        try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String auth_code = request.getParameter("code");
		
		if (DEBUG == true) {
			System.out.print(SERVLET_NAME + " Got redirect callback, auth_code is : " + auth_code + "\n");
		}
		
		Formatter fmt = new Formatter();
		fmt.format("%s?client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s", 
				TOKEN_BASE_URL, CLIENT_ID, CLIENT_SECRETE, auth_code, TOKEN_CALLBACK_BASE_URL);
		
		if (DEBUG == true) {
			System.out.print(SERVLET_NAME + " GET Token URL is : " + fmt.toString() + "\n");
		}
		
		URL url = new URL(fmt.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(30000);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		InputStreamReader isr = null;
		BufferedReader r = null;
		try {
			conn.connect();
			isr = new InputStreamReader(conn.getInputStream());
			r = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			if (conn.getResponseCode() == 200) {
				String s = "";
				while ((s = r.readLine()) != null) {
					sb.append(s);
				}
				
				if (DEBUG == true) {
					System.out.print(SERVLET_NAME + " Get Token result is : \n" + conn.getResponseCode() + "\n" + sb + "\n");
				}
				
				response.setStatus(conn.getResponseCode());
				response.getWriter().write(sb.toString());
			} else {
				response.setStatus(conn.getResponseCode());
				response.getWriter().write("ERROR Occurred !");
			}
		}
		catch (SocketTimeoutException e) {
			if (DEBUG == true) {
				System.out.print(SERVLET_NAME + " Get Token for code " + auth_code + " is timedout \n");
				e.printStackTrace(System.out);

			}
			response.setStatus(500);
			response.getWriter().write("Timeout occurred to fetch auth token !");
		}
		catch (IOException e) {
			if (DEBUG == true) {
				System.out.print(SERVLET_NAME + " Get Token for code " + auth_code + " is I/O error \n");
				e.printStackTrace(System.out);

			}
			response.setStatus(500);
			response.getWriter().write("Fail to fulfil valid authentication !");
		}
		finally {
			fmt.close();
			if (isr != null) {
				isr.close();
			}
			if (r != null) {
				r.close();
			}			
		}
//		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
