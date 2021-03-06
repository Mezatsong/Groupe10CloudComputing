/**
 * 
 */
package in4.cloudcomputing.groupe10;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ben.ladalja.DB;

/**
 * 
 * @author groupe10
 *
 */
@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {

	
	public AppServlet() {
		super();
	}
	
	public void init() throws ServletException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		DB.CONFIG_FILE_URL = getClass().getResource("db.properties").toString();
		DB.disableTransaction();
	}
	 
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		JSONArray array = new JSONArray();
		LocalDateTime now = LocalDateTime.now();
		try(ResultSet result = DB.table("messages").orderBy("date").get()) {
			while(result.next()) {
				JSONObject json = new JSONObject();
				json.put("username", result.getString("username"));
				json.put("content", result.getString("content"));
				LocalDateTime dt = new Timestamp(result.getLong("date")).toLocalDateTime();
				String dateStr = num(dt.getHour())+":"+num(dt.getMinute());
				if(dt.getDayOfYear() != now.getDayOfYear() || dt.getYear() != now.getYear()) {
					dateStr += " ("+num(dt.getDayOfMonth())+"-"+dt.getMonth().getDisplayName(TextStyle.SHORT, req.getLocale());
					if(dt.getYear() != now.getYear()) {
						dateStr += "-"+dt.getYear();
					}
					dateStr +=")";
				}
				json.put("date", dateStr);
				array.put(json);
			}
		} catch (JSONException | SQLException e) {
			e.printStackTrace();
		}
		resp.setContentType("application/json");
		resp.setCharacterEncoding( "UTF-8" );
		PrintWriter out = resp.getWriter();
		out.print( array.toString() );
		
		out.flush();
	
	}
	
	
	private String num(int num) {
		return ((num < 10)?"0":"")+String.valueOf(num);
	}

	

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		Map<String,Object> map = new HashMap<>();
		
		map.put("username", req.getParameter("username"));
		map.put("content", req.getParameter("content"));
		map.put("date", System.currentTimeMillis());
		DB.table("messages").insert(map);
		
		resp.setContentType("application/json");
		resp.setCharacterEncoding( "UTF-8" );
		PrintWriter out = resp.getWriter();
		out.print( new JSONObject().put("status", true).toString() );
	
	}

}
