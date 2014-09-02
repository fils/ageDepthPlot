/*
 * Created on Mar 2, 2005
 *
 */

/**
 * Direct representation of an entry in the Neptune datum table
 * @author gcb
 * @version 02-Mar-2005
 */

import java.util.*;

public class ADPNeptuneDatum {
	public int id;
	public int scale_id;
	public String fossil_group;
	public String label;
	public String type;
	public String name;
	public String taxon_id;
	public String age_min;
	public String age_max;
	public String qualifier;
	public String descript;
	
	/*
	 * Creates an ADPNeptuneDatum from a comma-delimited line . . . dumb, really
	 */
	public ADPNeptuneDatum( String buffy ) {
		StringTokenizer ronny = new StringTokenizer(buffy,",");
		try {
			id = Integer.parseInt( ronny.nextToken() );
		} catch (Exception ex) {
			id = -1;
		}
		try {
			scale_id = Integer.parseInt( ronny.nextToken() );
		} catch (Exception ex) {
			scale_id = -1;
		}
		fossil_group = ronny.nextToken();
		label = ronny.nextToken().trim();		
		type = ronny.nextToken().trim();
		name = ronny.nextToken().trim();
		taxon_id = ronny.nextToken().trim();
		age_min = ronny.nextToken();
		if ( age_min != null ) age_min = age_min.trim();
		age_max = ronny.nextToken();
		if ( age_max != null ) age_max = age_max.trim();
		qualifier = ronny.nextToken();
		if ( qualifier != null ) qualifier = qualifier.trim();
		descript = ronny.nextToken();
		if ( descript != null ) descript = descript.trim();
	}
	
	

}
