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

package jloda.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * An abstract base class for iterators with single element caching. Derived
 * classes need only implement the method <code>findNext</code>.
 */
public abstract class IteratorAdapter<T> implements Iterator<T> {
    private final LinkedList<T> cache = new LinkedList<>();

    /**
     * Returns the next available element or throws an exception.
     *
     * @return the next element.
     * @throws NoSuchElementException if no more elements are available.
     */
    protected abstract T findNext() throws NoSuchElementException;

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */

    public boolean hasNext() {
        if (cache.size() == 0) {
            try {
                cache.addLast(findNext());
            } catch (NoSuchElementException ex) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */

    public T next() {
        if (cache.size() == 0) {
            return findNext();
        } else {
            return cache.removeFirst();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */

    public void remove() {
        throw new UnsupportedOperationException("not supported");
    }
}
