package net.meanscript.java;
import net.meanscript.core.*;
public class MSJavaError {

	protected MSJavaError errorClass;
	protected String title;
	protected int id;
	
	private static int idCounter = 1;
	
	public MSJavaError(MSJavaError errorClass, String title) {
		super();
		this.errorClass = errorClass;
		this.title = title;
		this.id = idCounter++;
	}
}
