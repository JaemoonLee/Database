package dbinteract;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Random;
import com.alibaba.fastjson.JSON;

public class DBUtil {
	
	public static Connection dbConn(String name,String pwd) {
		Connection c=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			c=DriverManager.getConnection(
					"jdbc:oracle:thin:@oracle.cise.ufl.edu:1521:orcl",name,pwd);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return c;
	}

	public DBUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean addUser(String phone, String username, String password) {
		Connection con=null;
		boolean success=false;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			
			Statement sql=con.createStatement();
			ResultSet rs=sql.executeQuery(
					"select userid "
					+ "from common_users "
					+ "where userid='"+phone+"'");
			if(!rs.next()) {success=true;
					sql.executeUpdate("insert into common_users values("
							+"'"+phone+"','"+username+"','"+password+"')");}	
		} catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return success;
	}
	
	public static boolean login_verify(String username, String password) {
		Connection con=null;
		boolean success=false;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			ResultSet rs=sql.executeQuery(
					"select login_password "
					+ "from common_users "
					+ "where user_name='"+username+"'");
			if(!rs.next()) {success=false;}
			else {String pw=rs.getString(1);
				  if(pw.equals(password))success=true;}
		} catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return success;	
	}
	
	public static String getCategoryDescription(String category) {
		Connection con=null;
		String description=null;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			//System.out.println("Connection success");
			Statement sql=con.createStatement();
			String query="select description from category where category_name='"+category+"'";
			ResultSet rs=sql.executeQuery(query);
			//System.out.println("Query executed");
			if(rs.next()) {description=rs.getString(1);}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return description;
	}
	
	public static ArrayList<String> getFeaturedProj(){
		Connection con=null;
		ArrayList<String> info=new ArrayList<String>();
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select pro_id,slug,blurb,photo " + 
					"from proj " + 
					"where goal>=1000000 and " + 
					"status='A' and " + 
					"backer_num>=1000";
			ResultSet rs=sql.executeQuery(query);
			if(rs.next()) {
				for(int i=1;i<=4;i++) {
					info.add(rs.getString(i));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return info;
	}
	
	public static ArrayList<ArrayList<String>> getRecommendedProj(String featuredID){
		Connection con=null;
		ArrayList<ArrayList<String>> info=new ArrayList<ArrayList<String>>();
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query0="select count(*) " + 
					"from proj " + 
					"where pro_id<>'"+featuredID+"' and " + 
					"pledged>0 and " + 
					"status='A'";
			ResultSet rs0=sql.executeQuery(query0);
			int count=0;
			int select=0;
			Random rg1=new Random();
			Random rg2=new Random();
			if(rs0.next()) count=rs0.getInt(1);
			if(count<20) {
				select= rg1.nextInt(count)+1;
			}
			else select=rg1.nextInt(20)+1;
			String query1="select pro_id,slug,creator_id,x,photo from " + 
					"(select pro_id,slug,creator_id,x,photo,rownum rn from " + 
					"(select pro_id,slug,creator_id,pledged/goal as x,photo " + 
					"from proj " + 
					"where status='A' and pledged<>0 and pro_id<>'"+featuredID+"' " + 
					"order by pledged desc) R1) " + 
					"where rn="+select;
			ResultSet rs1=sql.executeQuery(query1);
			rs1.next();
			ArrayList<String> al1=new ArrayList<String>();
			String popularID1=rs1.getString(1);
			al1.add(popularID1);
			al1.add(rs1.getString(2));
			String query11="select user_name from common_users where userid='"+rs1.getString(3)+"'";
			String ratio1=(int)(rs1.getBigDecimal(4).floatValue()*100+0.5)+"%";
			String photo1=rs1.getString(5);
			ResultSet rs11=sql.executeQuery(query11);
			rs11.next();
			al1.add(rs11.getString(1));
			al1.add(ratio1);
			al1.add(photo1);
			info.add(al1);
			
			if(count<20)select=rg2.nextInt(count-1)+1;
			else select=rg2.nextInt(19)+1;
			String query2="select * from " + 
					"(select pro_id,slug,creator_id,x,photo,rownum rn from " + 
					"(select pro_id,slug,creator_id,pledged/goal as x,backer_num,photo " + 
					"from proj " + 
					"where status='A' and pro_id<>'" + featuredID + "' and "+
					"pro_id<>'"+popularID1+"' "+
					"order by backer_num desc) R1) " + 
					"where rn="+select;
			
			ResultSet rs2=sql.executeQuery(query2);
			rs2.next();
			ArrayList<String> al2=new ArrayList<String>();
			al2.add(rs2.getString(1));
			al2.add(rs2.getString(2));
			String query21="select user_name from common_users where userid='"+rs2.getString(3)+"'";
			String ratio2=(int)(rs2.getBigDecimal(4).floatValue()*100+0.5)+"%";
			String photo2=rs2.getString(5);
			ResultSet rs21=sql.executeQuery(query21);
			rs21.next();
			al2.add(rs21.getString(1));
			al2.add(ratio2);
			al2.add(photo2);
			info.add(al2);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return info;
	}
	
	public static String topCountries(){
		Connection con=null;
		List<Map<Object,Object>> info=new ArrayList<Map<Object,Object>>();
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select * from " + 
					"(select country, round(count(*)/(select count(*) from proj)*100,2) n " + 
					"from proj " + 
					"group by country " + 
					"order by n desc) " + 
					"where n>=1";
			ResultSet rs=sql.executeQuery(query);
			float sum=0;
			while(rs.next()) {
				HashMap<Object,Object> hm=new HashMap<Object,Object>();
				String country=rs.getString(1);
				float ratio=rs.getFloat(2);
				sum+=ratio;
				hm.put("label",country);
				hm.put("y",ratio);
				info.add(hm);
			}
			if(sum<100) {
				HashMap<Object,Object> hm=new HashMap<Object,Object>();
				hm.put("label","Other");
				hm.put("y",Math.round((100-sum)*100)/100.0);
				info.add(hm);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return JSON.toJSONString(info);		
	}
	
	public static int userNum() {
		Connection con=null;
		int n=0;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select count(*) from common_users";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			n=rs.getInt(1);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return n;
	}
	
	public static int categoryNum() {
		Connection con=null;
		int n=0;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select count(*) from category";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			n=rs.getInt(1);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return n;
	}
	
	public static int projNum() {
		Connection con=null;
		int n=0;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select count(*) from proj";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			n=rs.getInt(1);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return n;
	}
	
	public static int totalTupleNum() {
		Connection con=null;
		int n=0;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select sum(UT.num_rows) from user_tables UT";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			n=rs.getInt(1);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return n;
	}
	
	public static float successRate() {
		Connection con=null;
		float r=0;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select round(m.c/n.c*100,2) " + 
					"from " + 
					"(select count(*) c from proj where status='S') m, " + 
					"(select count(*) c from proj) n";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			r=rs.getFloat(1);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	public static int totalDonation() {
		Connection con=null;
		int d=0;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select sum(pledged) from proj where status='S'";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			d=rs.getInt(1);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return d;
	}
	
	public static String[] projDist(){
		Connection con=null;
		String[] r=new String[2];
		List<Map<Object,Object>> info1=new ArrayList<Map<Object,Object>>();
		List<Map<Object,Object>> info2=new ArrayList<Map<Object,Object>>();
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="with R as (select * from proj where ctgr is not null), " + 
					"R1 as " + 
					"(select ctgr c,count(pro_id) n " + 
					"from R " + 
					"where status='S' " + 
					"group by ctgr), " + 
					"R2 as " + 
					"(select ctgr c,count(pro_id) n " + 
					"from R " + 
					"where status='F' " + 
					"group by ctgr) " + 
					"select R1.c c,R1.n,R2.n " + 
					"from R1,R2 " + 
					"where R1.c=R2.c " + 
					"order by c asc";
			ResultSet rs=sql.executeQuery(query);
			while(rs.next()) {
				String ctgr=rs.getString(1);
				int sn=rs.getInt(2);
				int fn=rs.getInt(3);
				HashMap<Object,Object> hm=new HashMap<Object,Object>();
				hm.put("label",ctgr);
				hm.put("y",sn);
				info1.add(hm);
				hm=new HashMap<Object,Object>();
				hm.put("label",ctgr);
				hm.put("y",fn);
				info2.add(hm);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		r[0]=JSON.toJSONString(info1);
		r[1]=JSON.toJSONString(info2);
		return r;		
	}
	
	public static String numOfProjByIndividual() {
		Connection con=null;
		List<Map<Object,Object>> info=new ArrayList<Map<Object,Object>>();
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="with R as(select creator_id creator,count(pro_id) pronum " + 
					"from proj " + 
					"group by creator_id), " + 
					"R1 as(select count(*) from R " + 
					"where pronum<=1), " + 
					"R2 as(select count(*) from R " + 
					"where pronum>1 and pronum<=5), " + 
					"R3 as(select count(*) from R " + 
					"where pronum>5 and pronum<=20), " + 
					"R4 as(select count(*) from R " + 
					"where pronum>20) " + 
					"select * from R1,R2,R3,R4";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			HashMap<Object,Object> hm=new HashMap<Object,Object>();
			hm.put("label", "<=1");
			hm.put("y", rs.getInt(1));
			info.add(hm);
			hm=new HashMap<Object,Object>();
			hm.put("label", "<=5");
			hm.put("y", rs.getInt(2));
			info.add(hm);
			hm=new HashMap<Object,Object>();
			hm.put("label", "<=20");
			hm.put("y", rs.getInt(3));
			info.add(hm);
			hm=new HashMap<Object,Object>();
			hm.put("label", ">20");
			hm.put("y", rs.getInt(4));
			info.add(hm);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return JSON.toJSONString(info);
	}
	
	public static String[] annualAnalysis() {
		Connection con=null;
		String[] r=new String[2];
		List<Map<Object,Object>> info1=new ArrayList<Map<Object,Object>>();
		List<Map<Object,Object>> info2=new ArrayList<Map<Object,Object>>();
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="with R as(select * from " + 
					"(select to_char(lauchedat,'yyyy') y,count(pro_id) c " + 
					"from proj " + 
					"where status='S' or status='F' " + 
					"group by to_char(lauchedat,'yyyy'))), " + 
					"S as (select * from " + 
					"(select to_char(lauchedat,'yyyy') y,count(pro_id) c " + 
					"from proj " + 
					"where status='S' " + 
					"group by to_char(lauchedat,'yyyy'))) " + 
					"select R.y,R.c,round(S.c/R.c*100,2) ratio " + 
					"from R,S " + 
					"where R.y=S.y " + 
					"order by y asc";
			ResultSet rs=sql.executeQuery(query);
			while(rs.next()) {
				HashMap<Object,Object> hm=new HashMap<Object,Object>();
				int year=rs.getInt(1);
				int num=rs.getInt(2);
				float ratio=rs.getFloat(3);
				hm.put("x", year);
				hm.put("y", num);
				info1.add(hm);
				hm=new HashMap<Object,Object>();
				hm.put("x", year);
				hm.put("y", ratio);
				info2.add(hm);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		r[0]=JSON.toJSONString(info1);
		r[1]=JSON.toJSONString(info2);
		return r;
	}
	
	public static boolean startProj(String[] info) {
		Connection con=null;
		boolean success=true;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String proj_id=randString();
			while(true) {
				String query="select * from proj where pro_id='"+proj_id+"'";
				ResultSet rs=sql.executeQuery(query);
				if(!rs.next()) break;
				else proj_id=randString();}
			String creator_id="";
			String query="select userid from common_users where user_name='"+info[2]+"'";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			creator_id=rs.getString(1);
			SimpleDateFormat systime=new SimpleDateFormat("yyyy-MM-dd");
			String launchtime=systime.format(new Date());
			query="insert into proj values( " + 
					"'"+proj_id+"','"+info[0]+"','"+info[1]+"','"+creator_id+"','"+info[3]+"','"+info[4]+"',"+
					"to_date('"+launchtime+"','yyyy-MM-dd')," + "to_date('"+info[5]+"','yyyy-MM-dd'),"+info[6]+
					","+0+","+0+",'"+info[7]+"','"+info[8]+"',"+"'A')";
			sql.executeUpdate(query);
			
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
		return success;
	}
	
	public static ArrayList<ArrayList<String>> getPersonalProject(String name) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		Connection con=null;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select userid from common_users where user_name='"+name+"'";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			String creator_id=rs.getString(1);
			query="select pro_id,slug from proj where creator_id='"+creator_id+"'";
			rs=sql.executeQuery(query);
			while(rs.next()) {
				ArrayList<String> temp=new ArrayList<String>();
				temp.add(rs.getString(1));
				temp.add(rs.getString(2));
				result.add(temp);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static ArrayList<ArrayList<String>> getBackedProject(String name) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		Connection con=null;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select userid from common_users where user_name='"+name+"'";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			String backer_id=rs.getString(1);
			query="select proj_id from back where backer_id='"+backer_id+"'";
			rs=sql.executeQuery(query);
			while(rs.next()) {
				ArrayList<String> temp=new ArrayList<String>();
				temp.add(rs.getString(1));
				result.add(temp);
			}
			int L=result.size();
			for(int i=0;i<L;i++) {
				String id=result.get(i).get(0);
				query="select slug from proj where pro_id='"+id+"'";
				rs=sql.executeQuery(query);
				rs.next();
				result.get(i).add(rs.getString(1));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static ArrayList<String> getProjDetail(String projectID) {
		ArrayList<String> result = new ArrayList<String>();
		Connection con=null;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select * from proj where pro_id='"+projectID+"'";
			ResultSet rs=sql.executeQuery(query);
			rs.next();
			String proj_id=rs.getString(1);
			String slug=rs.getString(2);
			String ctgr=rs.getString(3);
			String creator_id=rs.getString(4);
			String creator="";
			String country=rs.getString(5);
			String loc=rs.getString(6);
			String lauchedat=rs.getDate(7).toString();
			String deadline=rs.getDate(8).toString();
			String goal=rs.getBigDecimal(9).doubleValue()+"";
			String pledged=rs.getBigDecimal(10).doubleValue()+"";
			String backer_num=rs.getInt(11)+"";
			String photo=rs.getString(12);
			String blurb=rs.getString(13);
			String status=rs.getString(14);
			if(status.equals("A"))status="Active";
			if(status.equals("S"))status="Success";
			if(status.equals("F"))status="failed";
			query="select user_name from common_users where userid='"+creator_id+"'";
			rs=sql.executeQuery(query);
			rs.next();
			creator=rs.getString(1);
			result.add(proj_id);
			result.add(slug);
			result.add(ctgr);
			result.add(creator);
			result.add(country);
			result.add(loc);
			result.add(lauchedat);
			result.add(deadline);
			result.add(goal);
			result.add(pledged);
			result.add(backer_num);
			result.add(photo);
			result.add(blurb);
			result.add(status);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static ArrayList<ArrayList<String>> getInfoByTime(String category){
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		Connection con=null;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select pro_id,slug,photo,rownum from " + 
					"(select pro_id,slug,photo " + 
					"from proj " + 
					"where status='A' and ctgr='" + category + "' " +
					"order by lauchedat desc) " + 
					"where rownum<=15";
			ResultSet rs=sql.executeQuery(query);
			rs=sql.executeQuery(query);
			while(rs.next()) {
				ArrayList<String> temp=new ArrayList<String>();
				temp.add(rs.getString(1));
				temp.add(rs.getString(2));
				temp.add(rs.getString(3));
				result.add(temp);
			}			
		} catch(Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static boolean backProj(String proj_id,String username,String amount) {
		Connection con=null;
		boolean success=true;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="select userid from common_users where user_name='"+username+"'";
			ResultSet rs=sql.executeQuery(query);
			rs=sql.executeQuery(query);
			rs.next();
			String userid=rs.getString(1);
			SimpleDateFormat systime=new SimpleDateFormat("yyyy-MM-dd");
			String back_time=systime.format(new Date());
			query="insert into back values('"+userid+"','"
					+proj_id+"',"+amount+","+"to_date('"+back_time+"','yyyy-MM-dd'))";
			sql.executeUpdate(query);
		} catch(Exception e) {
			e.printStackTrace();
			success=false;
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return success;
	}
	
	public static boolean alterProj(String[] info) {
		Connection con=null;
		boolean success=true;
		try {
			con=dbConn("gai","Gwy920728");
			if(con==null) {
				System.out.println("Connection fails");
				System.exit(0);
			}
			Statement sql=con.createStatement();
			String query="update proj set ctgr='"+info[1]+"',country='"+info[2]+"',"+
					"loc='"+info[3]+"',"+
					"deadline="+"to_date('"+info[4]+"','yyyy-mm-dd'),"+
					"goal="+info[5]+","+
					"photo='"+info[6]+"',"+
					"blurb='"+info[7]+"' "+
					"where pro_id='"+info[0]+"'";
			System.out.println(query);
			sql.executeUpdate(query);
		} catch(Exception e) {
			e.printStackTrace();
			success=false;
		} 
		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return success;
	}
	
	private static String randString() {
		String id="";
		for(int i=1;i<=15;i++) {
			int j=(int)(Math.random()*10);
			id=id+j;
		}
		return id;
	}
	
	public static void main(String[] args) {
		
		ArrayList<ArrayList<String>> al=getInfoByTime("Design & Tech");
		System.out.println(al.size());
		System.out.println(al.get(0).get(0));
		System.out.println(al.get(0).get(1));
		System.out.println(al.get(0).get(2));
		System.out.println(al.get(1).get(0));
		System.out.println(al.get(1).get(1));
		System.out.println(al.get(1).get(2));
		System.out.println(al.get(2).get(0));
		System.out.println(al.get(2).get(1));
		System.out.println(al.get(2).get(2));
		System.out.println(al.get(3).get(0));
		System.out.println(al.get(3).get(1));
		System.out.println(al.get(3).get(2));
	}

}
