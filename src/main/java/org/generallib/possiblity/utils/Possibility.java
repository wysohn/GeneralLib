/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.generallib.possiblity.utils;

import java.util.Random;

public class Possibility {
	public static void main(String[] ar){
		double possibility = 0.3;
		
		for(int x = 0; x < 5; x++){
			int win = 0,lose = 0;
			for(int i = 0; i < 1000; i++){
				if(isWin(possibility, 2))
					win++;
				else
					lose++;
			}
			
			System.out.println("win: "+win);
			System.out.println("lose: "+lose);
		}

	}
	
	/**
	 * 1/(2(level - vert))
	 * 
	 * @param level
	 * @return 1.0 if x - vert <= 0; 1/(2^(level - vert)) otherwise
	 */
	public static double getReciprocalPossibility(int vertAsymtote, int level){
		int base = level - vertAsymtote;
		
		if(base > 0){
			return 1.0D/(pow(2, base));
		}else{
			return 1.0D;
		}
	}
	
	private static final Random rand = new Random();
	/**
	 * 
	 * @param possiblity possibility to test 0 ~ 1
	 * @param decimals limit of decimals (for example, decimals 2 means (int)(possibility * 10^2))
	 * @return true if win; false if not
	 */
	public static boolean isWin(double possiblity, int decimals){
		int multiplier = pow(10, decimals);
		double range = (int)Math.round(possiblity * multiplier);
		
		int numTest = rand.nextInt(multiplier);
		
		//check if the random num is 0 out of possible numbers
		return numTest < range;
	}
	
	/**
	 * 
	 * @param num
	 * @param n
	 * @return num ^ n
	 */
	private static int pow(int num, int n){
		if(n == 0)
			return 1;
		
		int sum = num;
		for(int i = 1; i < n; i++){
			sum *= num;
		}
		return sum;
	}
}