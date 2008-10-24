package edu.tum.cs.vis;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import edu.tum.cs.tools.MySQLConnection;
import processing.core.PApplet;

public class STIsomap extends AnimatedCanvas {

	private static final long serialVersionUID = 1L;

	@Override
	public void setup() {
		String host = "atradig131";
		String db = "stt-human-import";
		String user = "tenorth";
		String password = "UEY9KbNb";
		
		// coordinate system
		add(new CoordinateSystem(500));
		
		// 3D trajectory
		Trajectory traj = new Trajectory();
		addAnimated(traj);
		//add(traj);
		try {
			MySQLConnection conn = new MySQLConnection(host, user, password, db);
			ResultSet rs = conn
					.select("select x,y,z from STT_DETAILED_ABSTRACT_EXP_ISOMAP3D_INTERVAL where episode_nr=0 and occurrence_nr=1 order by instance_nr");
			while (rs.next()) {
				float x = rs.getFloat(1) / 4000 * 500;
				float y = rs.getFloat(2) / 4000 * 500;
				float z = rs.getFloat(3) / 4000 * 500;
				System.out.printf("%f/%f/%f\n", x, y, z);
				traj.addPoint(x, y, z);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		
		traj.segment();
		
		super.setup();
	}
	
	public static void main(String[] args) {
		PApplet.main(new String[] { STIsomap.class.getCanonicalName()  });
	}
}