/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.acconsulting.factory.exception;

/**
 *
 * @author Admin
 */
public class FactoryCreationException extends Exception{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7373744754135110586L;

	public FactoryCreationException() {
		
		super();
	}

	public FactoryCreationException(String message) {
		
		super(message);
	}
	


	public FactoryCreationException(Throwable cause) {
		
		super(cause);
	}
	
	public FactoryCreationException(String message, Throwable cause) {
		
		super(message, cause);
	}

}


