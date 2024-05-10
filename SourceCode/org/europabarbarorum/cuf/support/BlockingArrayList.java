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

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * This calls wraps a {@link ArrayList} and guards access to it with a {@link Semaphore}.
 * The result allows different threads to modify the same list, waiting for 
 * any pending modifications to complete. Since the {@link Semaphore} is fair, 
 * operations on the list run on a FIFO basis.
 * @param <E> the type of elements stored in the wrapped {@link ArrayList}.
 * @author Johan Ouwerkerk
 */
public class BlockingArrayList<E> {

    /**
     * Interface to define an operation with a given result type
     * as callback with a given type of operand.
     * @param <R> the return type of this operation.
     * @param <T> the type of operand.
     */
    public static interface Operator<R, T> {

        /**
         * Perform the operation.
         * @param operand the argument to this {@link Operator}.
         * @return the result of this operation.
         */
        public R callback (T operand);
    }
    private final ArrayList<E> list;
    private final Semaphore lock = new Semaphore(1, true);

    /**
     * Create a new {@link BlockingArrayList}.
     */
    @SuppressWarnings("unchecked")
    public BlockingArrayList () {
        list = new ArrayList();
    }

    /**
     * Create a new {@link BlockingArrayList}.
     * @param capacity the initial capacity of the wrapped {@link ArrayList} to use.
     */
    @SuppressWarnings("unchecked")
    public BlockingArrayList (int capacity) {
        list = new ArrayList(capacity);
    }


    /**
     * Get the index of an element in the wrapped {@link ArrayList}.
     * @param element the element to look up
     * @return the index at which the given element is located, or -1 if it is not found in the
     * wrapped {@link ArrayList}
     * @throws InterruptedException if the thread is interrupted.
     */
    public int indexOf (final E element) throws InterruptedException {
        return operation(new Operator<Integer, ArrayList<E>>() {

            @Override
            public Integer callback (ArrayList<E> operand) {
                return operand.indexOf(element);
            }
        });
    }

    /**
     * Get the number of elements in the wrapped {@link ArrayList}.
     * @return the number of elements in the wrapped {@link ArrayList}.
     * @throws InterruptedException if the thread is interrupted
     */
    public int size () throws InterruptedException {
        return operation(new Operator<Integer, ArrayList<E>>() {

            @Override
            public Integer callback (ArrayList<E> operand) {
                return operand.size();
            }
        });
    }

    /**
     * Get an element from the wrapped {@link ArrayList}.
     * @param index the index of the element to get.
     * @return the element located at the specified index.
     * @throws InterruptedException if the thread is interrupted.
     */
    public E get (final int index) throws InterruptedException {
        return operation(new Operator<E, ArrayList<E>>() {

            @Override
            public E callback (ArrayList<E> operand) {
                return operand.get(index);
            }
        });
    }

    /**
     * Add an element to the wrapped {@link ArrayList}.
     * @param element the element to add.
     * @return true.
     * @throws InterruptedException if the thread is interrupted.
     */
    public boolean add (final E element) throws InterruptedException {
        return operation(new Operator<Boolean, ArrayList<E>>() {

            @Override
            public Boolean callback (ArrayList<E> operand) {
                return operand.add(element);
            }
        });
    }

    /**
     * Remove an element from the wrapped {@link ArrayList}.
     * @param element the element to remove.
     * @return true if the wrapped {@link ArrayList} contains the specified element.
     * @throws InterruptedException if the thread is interrupted.
     */
    public boolean remove (final E element) throws InterruptedException {
        return operation(new Operator<Boolean, ArrayList<E>>() {

            @Override
            public Boolean callback (ArrayList<E> operand) {
                return operand.remove(element);
            }
        });
    }

    /**
     * Remove an element from the wrapped {@link ArrayList}.
     * @param index the index of the element to remove.
     * @return the element which used to be at the specified index.
     * @throws InterruptedException if the thread is interrupted.
     */
    public E remove (final int index) throws InterruptedException {
        return operation(new Operator<E, ArrayList<E>>() {

            @Override
            public E callback (ArrayList<E> operand) {
                return operand.remove(index);
            }
        });
    }

    /**
     * Perform an arbitrary {@link Operator operation} on the wrapped {@link ArrayList}.
     * @param <R> the return type of the given operation.
     * @param operation the {@link Operator operation} to perform.
     * @return the result of the given operation with the wrapped {@link ArrayList} as
     * operand.
     * @throws InterruptedException if the thread is interrupted.
     */
    public <R> R operation (Operator<R, ArrayList<E>> operation) throws
            InterruptedException {
        try {
            lock.acquire();
            R result = operation.callback(list);
            return result;
        }
        finally {
            lock.release();
        }
    }
}
