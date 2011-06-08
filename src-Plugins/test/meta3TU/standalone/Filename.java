package test.meta3TU.standalone;

/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  - Neither the name of Sun Microsystems nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * This class assumes that the string used to initialize fullPath has a
 * directory path, filename, and extension. The methods won't work if it
 * doesn't.
 */
class Filename {	
  private String fullPath;
  private char pathSeparator, extensionSeparator;

  public Filename(String str) {
	  this(str, FileUtils.DEFAULT_PATH_SEPARATOR, FileUtils.DEFAULT_EXTENSION_SEPARATOR);
  }
  
  public Filename(String str, char sep, char ext) {
    fullPath = str.replace('/', sep).replace('\\', sep);
    pathSeparator = sep;
    extensionSeparator = ext;
  }

  public String extension() {
    int dot = fullPath.lastIndexOf(extensionSeparator);
    	
    if (dot < 0 || (dot == (fullPath.length() - 1)))
    	return "";
    else if (fullPath.substring(dot + 1).equalsIgnoreCase("gz")
    	&& (dot > 0))
    {
    	int secondDot = fullPath.lastIndexOf(extensionSeparator, dot - 1);
        if (secondDot < 0 || (secondDot == (dot - 1)))
        	return "gz";
        else
    	   	return fullPath.substring(secondDot + 1);
    }
    else
	   	return fullPath.substring(dot + 1);    	
  }

  public String filename() { // gets filename without extension
    int sep = fullPath.lastIndexOf(pathSeparator);
    
    String filenameWithExtension = fullPath;
    if (sep >= 0)    
    	filenameWithExtension = fullPath.substring(sep + 1);

    int dot = filenameWithExtension.lastIndexOf(extensionSeparator);
    if (dot < 0)
    	return filenameWithExtension;
    else if (filenameWithExtension.substring(dot + 1).equalsIgnoreCase("gz")
    	&& (dot > 0))
    {
    	int secondDot = filenameWithExtension.lastIndexOf(extensionSeparator, dot - 1);
    	if (secondDot < 0)
    		return filenameWithExtension.substring(0, dot);
    	else
    		return filenameWithExtension.substring(0, secondDot);
    }
    else
    	return filenameWithExtension.substring(0, dot);
  }

  public String filenameWithExtension() { // gets filename without extension
    int sep = fullPath.lastIndexOf(pathSeparator);
    
    String filenameWithExtension = fullPath;
    if (sep >= 0)    
    	filenameWithExtension = fullPath.substring(sep + 1);  
    
    return filenameWithExtension;
  }

  public String path() {
    int sep = fullPath.lastIndexOf(pathSeparator);
    if (sep < 0)
    	return "";
    else 
    	return fullPath.substring(0, sep);
  }
}
