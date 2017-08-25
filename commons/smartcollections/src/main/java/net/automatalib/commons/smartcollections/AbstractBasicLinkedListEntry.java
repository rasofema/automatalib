/* Copyright (C) 2013-2017 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.commons.smartcollections;

/**
 * Abstract base class for entries in a linked list. Takes care for handling predecessor and successor, but not storage
 * of the element itself.
 *
 * @param <E>
 *         element class.
 * @param <T>
 *         linked list entry class.
 *
 * @author Malte Isberner
 */
public abstract class AbstractBasicLinkedListEntry<E, T extends AbstractBasicLinkedListEntry<E, T>> implements LinkedListEntry<E, T> {

    // predecessor and successor
    private T prev, next;

    /*
     * (non-Javadoc)
     * @see de.ls5.collections.LinkedListEntry#getPrev()
     */
    @Override
    public T getPrev() {
        return prev;
    }

    /*
     * (non-Javadoc)
     * @see de.ls5.collections.LinkedListEntry#setPrev(de.ls5.collections.LinkedListEntry)
     */
    @Override
    public void setPrev(T prev) {
        this.prev = prev;
    }

    /*
     * (non-Javadoc)
     * @see de.ls5.collections.LinkedListEntry#getNext()
     */
    @Override
    public T getNext() {
        return next;
    }

    /*
     * (non-Javadoc)
     * @see de.ls5.collections.LinkedListEntry#setNext(de.ls5.collections.LinkedListEntry)
     */
    @Override
    public void setNext(T next) {
        this.next = next;
    }
}