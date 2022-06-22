/**
 *
 */
package org.opensrp.connector.domain;

import org.ektorp.support.TypeDiscriminator;

/**
 * @author Samuel Githengi created on 03/19/20
 */
@TypeDiscriminator("doc.type == 'Camp'")
public class Camp extends org.opensrp.domain.Camp {

}
