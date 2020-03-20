/**
 * 
 */
package org.opensrp.connector.domain;

import org.ektorp.support.TypeDiscriminator;

/**
 * @author Samuel Githengi created on 03/19/20
 */

@TypeDiscriminator("doc.type == 'DHIS2Marker'")
public class DHIS2Marker extends org.opensrp.domain.DHIS2Marker{
	
}
