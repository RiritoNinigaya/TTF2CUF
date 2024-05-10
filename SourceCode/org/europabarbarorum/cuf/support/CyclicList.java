/*
 *
 *  Copyright (C) 2010, 2011, 2012 The Europa Barbarorum Team
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of The Europa Barbarorum Team nor the
 *        names of other contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL The Europa Barbarorum Team BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.europabarbarorum.cuf.support;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * A fixed size list backed by array that overwrites the first entries when new
 * entries are added to a full list.
 * @param <E> the type of object to store in the list
 * @author Johan Ouwerkerk
 */
public class CyclicList<E> extends AbstractList<E> {

    /**
     * Array backing the list.
     */
    private final Object[] list;
    /**
     * “Cursor” of this list.
     */
    private int cursor;

    /**
     * Create a new {@link CyclicList} with the given size.
     * @param allocSize the size of the list to create.
     */
    public CyclicList (int allocSize) {

        list = new Object[allocSize];
        cursor = 0;
    }

    /**
     * Get the element at the given index.
     * @param index the index of the element to fetch.
     * @return the element at the given index.
     * @see #mapIndex(int)
     */
    @Override
    @SuppressWarnings("unchecked")
    public E get (int index) {
        return (E) list[mapIndex(index)];
    }

    /**
     * Auxilliary method to map an input index using the modulus operator.
     * @param index the index to map
     * @return the corresponding index in the array backing this list.
     */
    public int mapIndex (int index) {
        return (begin() + index) % list.length;
    }

    /**
     * Checks if a given index corresponds to a valid value in the array backing the list.
     * @param index the index to check
     * @return true if the value in the array backing this list is not null, false otherwise.
     * @see #mapIndex(int)
     */
    public boolean isValid (int index) {
        return list[mapIndex(index)] != null;
    }

    /**
     * Get the size of this {@link CyclicList}.
     * @return the length of the array backing this list
     */
    @Override
    public int size () {
        return list.length;
    }

    /**
     * Get the “cursor” of this list. This value corresponds to the position (index) at which the {@link #add(java.lang.Object) } operation would insert
     * the next element.
     * @return the value of {@link #cursor}
     */
    public int cursor () {
        return cursor;
    }

    /**
     * Get an iterator for use in for each loops. This iterator does not support the {@link Iterator#remove() }
     * method.
     * @return the iterator that iterates over all values in the array.
     */
    @Override
    public Iterator<E> iterator () {
        return new Iterator<E>() {

            private int i = 0;
            private int max = count();

            @Override
            public boolean hasNext () {
                return i < max && isValid(i);
            }

            @Override
            public E next () {
                E next = get(i);
                ++i;
                return next;
            }

            @Override
            public void remove () {
                throw new NotEditableException();
            }
        };
    }

    /**
     * Return whether or not the list is empty.
     * @return whether or not the element at the {@link #cursor() } is null.
     */
    @Override
    public boolean isEmpty () {
        return list[cursor] == null;
    }

    /**
     * Get the number of elements in this list. This method has a simple check to determine this number:
     * <ul>
     * <li>If the zeroth element of the backing array is null, return {@link #cursor() }</li>
     * <li>If not return {@link #size()}.
     * </ul>
     * @return the number of elements in this list ignoring those that have been overwritten.
     */
    public int count () {
        return list[0] == null ? cursor : list.length;
    }

    /**
     * The element that represents the start of the list, ignoring any elements that have been overwritten.
     * This method has a simple check to determine this number:
     * <ul>
     * <li>If the zeroth element of the backing array is null, return 1</li>
     * <li>If not return <code>1 + {@link #cursor() }.</li>
     * </ul>
     * @return the index that is considered the start of this list.
     */
    private int begin () {
        return list[0] == null ? 1 : cursor + 1;
    }

    /**
     * Add an element at the end of the {@link CyclicList}.
     * @param e the element to add.
     * @return true as per the spec of {@link AbstractList}.
     */
    @Override
    public boolean add (E e) {
        ++cursor;
        cursor %= list.length;
        list[cursor] = e;
        return true;
    }

    /**
     * Sets the given element at the given index.
     * @param e the element to set
     * @param index the index of the element to set
     * @return the element previously at the given index, possibly null.
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set (int index, E e) {
        int k = mapIndex(index);
        Object old = list[k];
        list[k] = e;
        return old == null ? null : (E) old;
    }

    /**
     * Get the element that was added last in this list.
     * @return the element that was added last.
     */
    @SuppressWarnings("unchecked")
    public E getLast () {
        Object o = list[cursor];
        return o == null ? null : (E) o;
    }
    /**
     * Sets an element at the position of the element which was added last to this list.
     * @param elem the element to replace the last element with.
     * @return the element which was added last to this list, possibly null. 
     * If this list contains no elements yet then this method also returns null.
     */
    public E setLast(E elem) {
        E old = getLast();
        list[cursor] = elem;
        return old;
    }
}
