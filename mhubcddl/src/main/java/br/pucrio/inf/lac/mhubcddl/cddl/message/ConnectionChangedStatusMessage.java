/**
 * 
 */
package br.pucrio.inf.lac.mhubcddl.cddl.message;

import java.io.Serializable;

/**
 * @author bertodetacio
 *
 */
public class ConnectionChangedStatusMessage extends LivelinessMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int CLIENT_CONNECTED = 1;
	
	public static final int CLIENT_SELF_DESCONNECTED = 2;

	public static final int CLIENT_DESCONNECTED_FOR_FAILURE = 3;
	
	private int status = CLIENT_CONNECTED;
	
	/**
	 * 
	 */
	public ConnectionChangedStatusMessage() {
		super();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		
		if(status > CLIENT_DESCONNECTED_FOR_FAILURE || status < CLIENT_CONNECTED){
			throw new IllegalArgumentException("O valor não pode ser maior que "+ CLIENT_SELF_DESCONNECTED +" e nem menor que "+ CLIENT_CONNECTED);
		}
		
		this.status = status;
	}

}
