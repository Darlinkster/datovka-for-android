/*
Datovka - An Android client for Datove schranky
    Copyright (C) 2012  CZ NIC z.s.p.o. <podpora at nic dot cz>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cz.nic.datovka.tinyDB.exceptions;

public class StreamInterruptedException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public StreamInterruptedException(String message){
		super(message);
	}

}
