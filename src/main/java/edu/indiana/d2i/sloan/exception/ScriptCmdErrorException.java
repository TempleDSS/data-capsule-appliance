/*******************************************************************************
 * Copyright 2014 The Trustees of Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.indiana.d2i.sloan.exception;

/**
 * 
 * Thrown when the script returns a non-zero exit code
 * 
 */
public class ScriptCmdErrorException extends Exception {

	private static final long serialVersionUID = -6337023739467662121L;

	public ScriptCmdErrorException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ScriptCmdErrorException(String message) {
		super(message);
	}
}
