/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.model;


public class Survey {

	private String q1_response;
	private String q2_response;
	private String userId;
	private String icon;
	
	public String getQOneResponse() {
		return q1_response;
	}
	
	public void setQOneResponse(String q1_response) {
		this.q1_response = q1_response;
	}
	
	public String getQTwoResponse() {
		return q2_response;
	}
	
	public void setQTwoResponse(String q2_response) {
		this.q2_response = q2_response;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
}
