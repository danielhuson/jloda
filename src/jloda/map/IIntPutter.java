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

package jloda.map;

/**
 * A read and write long-indexed array of ints
 * Daniel Huson, 3.2015
 */
public interface IIntPutter {
    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    int get(long index);

    /**
     * puts value for given index
     *
     * @param index
     * @param value return the putter
     */
    void put(long index, int value);

    /**
     * length of array
     *
     * @return array length
     * @throws java.io.IOException
     */
    long limit();

    /**
     * close the array
     */
    void close();
}
