/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.progs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * approximate square root of 2
 * Daniel Huson, 12.2011
 */
public class ApproximateSquareRootOf2 {
    /**
     * approximate square root of two
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // print prompt:
        System.out.println("Approximation of square root of 2");
        System.out.println("Using a=0 and b=2");
        System.out.print("Enter max error: ");
        System.out.flush();
        // get parameters:
        double maxError = Double.parseDouble((new BufferedReader(new InputStreamReader(System.in))).readLine());

        // run algorithm:
        double a = 0, b = 2;
        while (b - a > maxError) {
            double c = (a + b) / 2;

            System.out.println(String.format("a=%1.12g b=%1.12g   c=%1.12g   b-a=%g", a, b, c, b - a));

            if (c * c < 2)
                a = c;
            else
                b = c;
        }
        // output:
        System.out.println("Approximation: " + a);
    }
}
