/*

Copyright (C) SYSTAP, LLC 2006-2008.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/
/*
 * Created on Jun 10, 2008
 */

package com.bigdata.btree;

import java.util.NoSuchElementException;
import java.util.UUID;

import junit.framework.TestCase2;

import com.bigdata.journal.TemporaryRawStore;

/**
 * Abstract base class for {@link ITupleCursor} test suites.
 * 
 * @todo also run tests against the FusedView and the scale-out federation
 *       variant (progressive forward or reverse scan against a partitioned
 *       index).
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
abstract public class AbstractCursorTestCase extends TestCase2 {

    /**
     * 
     */
    public AbstractCursorTestCase() {
    }

    /**
     * @param arg0
     */
    public AbstractCursorTestCase(String arg0) {
        super(arg0);
    }

    /**
     * Create an appropriate cursor instance for the given B+Tree.
     * 
     * @param btree
     * @param flags
     * @param fromKey
     * @param toKey
     * 
     * @return An {@link ITupleCursor} for that B+Tree.
     */
    abstract protected ITupleCursor<String> newCursor(AbstractBTree btree, int flags,
            byte[] fromKey, byte[] toKey);
    
    /**
     * Create an appropriate cursor instance for the given B+Tree.
     * 
     * @param btree
     * 
     * @return
     */
    protected ITupleCursor<String> newCursor(AbstractBTree btree) {

        return newCursor(btree, IRangeQuery.DEFAULT, null/* fromKey */, null/* toKey */);
        
    }
    
    /**
     * Return a B+Tree populated with data for
     * {@link #doBaseCaseTest(IndexSegment)}
     */
    protected BTree getBaseCaseBTree() {

        BTree btree = BTree.create(new TemporaryRawStore(), new IndexMetadata(
                UUID.randomUUID()));

        btree.insert(10, "Bryan");
        btree.insert(20, "Mike");
        btree.insert(30, "James");

        return btree;

    }

    /**
     * Test helper tests first(), last(), next(), prior(), and seek() given a
     * B+Tree that has been pre-popluated with some known tuples.
     * 
     * @param btree
     *            The B+Tree.
     * 
     * @see #getBaseCaseBTree()
     */
    protected void doBaseCaseTest(AbstractBTree btree) {

        // test first()
        {

            ITupleCursor<String> cursor = newCursor(btree, IRangeQuery.DEFAULT,
                    null/* fromKey */, null/* toKey */);

            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.first());

        }

        // test last()
        {

            ITupleCursor<String> cursor = newCursor(btree, IRangeQuery.DEFAULT,
                    null/* fromKey */, null/* toKey */);

            assertEquals(new TestTuple<String>(30, "James"), cursor.last());

        }

        // test tuple()
        {

            ITupleCursor<String> cursor = newCursor(btree, IRangeQuery.DEFAULT,
                    null/* fromKey */, null/* toKey */);

            // current tuple is initially not defined.
            assertNull(cursor.tuple());

            // defines the current tuple.
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.first());

            // same tuple.
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());

        }

        // test next()
        {

            ITupleCursor<String> cursor = newCursor(btree, IRangeQuery.DEFAULT,
                    null/* fromKey */, null/* toKey */);

            assertTrue(cursor.hasNext());

            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.next());

            assertTrue(cursor.hasNext());

            assertEquals(new TestTuple<String>(20, "Mike"), cursor.next());

            assertTrue(cursor.hasNext());

            assertEquals(new TestTuple<String>(30, "James"), cursor.next());

            // itr is exhausted.
            assertFalse(cursor.hasNext());

            // the cursor position is still defined.
            assertTrue(cursor.isCursorPositionDefined());
            
            // itr is exhausted.
            try {
                cursor.next();
                fail("Expecting " + NoSuchElementException.class);
            } catch (NoSuchElementException ex) {
                log.info("Ignoring expected exception: " + ex);
            }

            // make sure that the iterator will not restart.
            assertFalse(cursor.hasNext());
            
            // make sure that the iterator will not restart.
            try {
                cursor.next();
                fail("Expecting " + NoSuchElementException.class);
            } catch (NoSuchElementException ex) {
                log.info("Ignoring expected exception: " + ex);
            }
            
        }

        // test prior()
        {

            ITupleCursor<String> cursor = newCursor(btree, IRangeQuery.DEFAULT,
                    null/* fromKey */, null/* toKey */);

            assertTrue(cursor.hasPrior());

            assertEquals(new TestTuple<String>(30, "James"), cursor.prior());

            assertTrue(cursor.hasPrior());

            assertEquals(new TestTuple<String>(20, "Mike"), cursor.prior());

            assertTrue(cursor.hasPrior());

            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.prior());

            // itr is exhausted.
            assertFalse(cursor.hasPrior());

            // the cursor position is still defined.
            assertTrue(cursor.isCursorPositionDefined());

            // itr is exhausted.
            try {
                cursor.prior();
                fail("Expecting " + NoSuchElementException.class);
            } catch (NoSuchElementException ex) {
                log.info("Ignoring expected exception: " + ex);
            }

            // make sure that the iterator will not restart.
            assertFalse(cursor.hasPrior());

            // make sure that the iterator will not restart.
            try {
                cursor.prior();
                fail("Expecting " + NoSuchElementException.class);
            } catch (NoSuchElementException ex) {
                log.info("Ignoring expected exception: " + ex);
            }

        }

        /*
         * test seek(), including prior() and next() after a seek()
         */
        {

            ITupleCursor<String> cursor = newCursor(btree, IRangeQuery.DEFAULT,
                    null/* fromKey */, null/* toKey */);

            // probe(30)
            assertEquals(new TestTuple<String>(30, "James"), cursor.seek(30));
            assertFalse(cursor.hasNext());
            assertTrue(cursor.hasPrior());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.prior());

            // probe(10)
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.seek(10));
            assertFalse(cursor.hasPrior());
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.next());

            // probe(20)
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.seek(20));
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(30, "James"), cursor.next());

            assertEquals(new TestTuple<String>(20, "Mike"), cursor.seek(20));
            assertTrue(cursor.hasPrior());
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.prior());

        }

        /*
         * test seek() when the probe key is not found / visitable.
         * 
         * this also tests prior() and next() after the seek to a probe key that
         * does not exist in the index.
         */
        {

            ITupleCursor<String> cursor = newCursor(btree);

            // seek to a probe key that does not exist.
            assertEquals(null, cursor.seek(29));
            assertEquals(null, cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(29),cursor.currentKey());
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(30, "James"), cursor.next());
            assertEquals(new TestTuple<String>(30, "James"), cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(30),cursor.currentKey());
            assertFalse(cursor.hasNext());
            assertTrue(cursor.hasPrior());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.prior());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(20),cursor.currentKey());

            // seek to a probe key that does not exist.
            assertEquals(null, cursor.seek(9));
            assertEquals(null, cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(9),cursor.currentKey());
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.next());
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(10),cursor.currentKey());
            assertFalse(cursor.hasPrior());
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.next());
            assertEquals(KeyBuilder.asSortKey(20),cursor.currentKey());

            // seek to a probe key that does not exist and scan forward.
            assertEquals(null, cursor.seek(19));
            assertEquals(null, cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(19),cursor.currentKey());
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.next());
            assertEquals(new TestTuple<String>(20, "Mike"), cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(20),cursor.currentKey());
            assertTrue(cursor.hasNext());
            assertEquals(new TestTuple<String>(30, "James"), cursor.next());
            assertEquals(new TestTuple<String>(30, "James"), cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(30),cursor.currentKey());

            // seek to a probe key that does not exist and scan backward.
            assertEquals(null, cursor.seek(19));
            assertEquals(null, cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(19),cursor.currentKey());
            assertTrue(cursor.hasPrior());
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.prior());
            assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());
            assertEquals(KeyBuilder.asSortKey(10),cursor.currentKey());
            assertFalse(cursor.hasPrior());

            // seek to a probe key that does not exist (after all valid tuples).
            assertEquals(null, cursor.seek(31));
            assertEquals(null, cursor.tuple());
            assertTrue(cursor.isCursorPositionDefined());
            assertEquals(KeyBuilder.asSortKey(31),cursor.currentKey());
            assertFalse(cursor.hasNext());

            // seek to a probe key that does not exist (after all valid tuples).
            assertEquals(null, cursor.seek(31));
            assertEquals(null, cursor.tuple());
            assertTrue(cursor.isCursorPositionDefined());
            assertEquals(KeyBuilder.asSortKey(31),cursor.currentKey());
            assertTrue(cursor.hasPrior());
            assertEquals(new TestTuple<String>(30, "James"), cursor.prior());

        }

        /*
         * Test to verify that optional range constraints are correctly imposed,
         * including when the inclusive lower bound and the exclusive upper
         * bound correspond to tuples actually present in the B+Tree.
         */
        {

            /*
             * The inclusive lower bound (fromKey) is on a tuple that exists in
             * the B+Tree (the first tuple).
             * 
             * The exclusive upper bound (toKey) is on a tuple that exists and
             * which is the successor of the first tuple.
             * 
             * The cursor should only visit the first tuple.
             */
            {
             
                final byte[] fromKey = KeyBuilder.asSortKey(10);
                
                final byte[] toKey = KeyBuilder.asSortKey(20);

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertTrue(cursor.hasNext());
                
                assertEquals(new TestTuple<String>(10,"Bryan"),cursor.next());
                
                assertFalse(cursor.hasNext());

                // now seek to the last tuple.
                assertEquals(new TestTuple<String>(10,"Bryan"),cursor.last());

                assertFalse(cursor.hasNext());
                assertFalse(cursor.hasPrior());

                // accessible via seek()
                assertEquals(new TestTuple<String>(10,"Bryan"),cursor.seek(10));

                // not accessible via seek().
                try {
                    cursor.seek(20);
                    fail("Expecting: "+IllegalArgumentException.class);
                } catch(IllegalArgumentException ex) {
                    log.info("Ignoring expected exception: "+ex);
                }
                
            }
            
            /*
             * The inclusive lower bound (fromKey) is on a tuple that exists in
             * the B+Tree (the second tuple).
             * 
             * The exclusive upper bound (toKey) is on a tuple that exists in
             * the B+Tree (the third and last tuple).
             * 
             * The cursor should only visit the 2nd tuple.
             */
            {
             
                final byte[] fromKey = KeyBuilder.asSortKey(20);
                
                final byte[] toKey = KeyBuilder.asSortKey(30);

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertTrue(cursor.hasNext());
                
                assertEquals(new TestTuple<String>(20,"Mike"),cursor.next());
                
                assertFalse(cursor.hasNext());
                assertFalse(cursor.hasPrior());

                // now seek to the last tuple.
                assertEquals(new TestTuple<String>(20,"Mike"),cursor.last());

                assertFalse(cursor.hasNext());
                assertFalse(cursor.hasPrior());

                // accessible via seek()
                assertEquals(new TestTuple<String>(20,"Mike"),cursor.seek(20));

                // not accessible via seek().
                try {
                    cursor.seek(10);
                    fail("Expecting: "+IllegalArgumentException.class);
                } catch(IllegalArgumentException ex) {
                    log.info("Ignoring expected exception: "+ex);
                }
                
                // not accessible via seek().
                try {
                    cursor.seek(30);
                    fail("Expecting: "+IllegalArgumentException.class);
                } catch(IllegalArgumentException ex) {
                    log.info("Ignoring expected exception: "+ex);
                }
                
            }

        }
        
    }

    /**
     * Test helper tests for fence posts when the index is empty
     * <p>
     * Note: this test can not be written for an {@link IndexSegment} since you
     * can't have an empty {@link IndexSegment}.
     * 
     * @param btree
     *            An empty B+Tree.
     */
    protected void doEmptyIndexTest(AbstractBTree btree) {

        /*
         * Test with no range limits.
         */
        {

            // first()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.first());

                // since there was nothing visitable the cursor position NOT
                // defined
                assertFalse(cursor.isCursorPositionDefined());

                // no current key.
                assertEquals(null, cursor.currentKey());

                assertNull(cursor.tuple());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // last()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.last());

                // since there was nothing visitable the cursor position NOT
                // defined
                assertFalse(cursor.isCursorPositionDefined());

                // no current key.
                assertEquals(null, cursor.currentKey());

                assertNull(cursor.tuple());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // tuple()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.tuple());

            }

            // hasNext(), next().
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertFalse(cursor.hasNext());

                try {
                    cursor.next();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // hasPrior(), prior().
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertFalse(cursor.hasPrior());

                try {
                    cursor.prior();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // seek()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.seek(1));

                assertFalse(cursor.hasPrior());

                assertFalse(cursor.hasNext());

            }

        }

        /*
         * Test with range limit. Since there is no data in the index the actual
         * range limits imposed matter very little.
         */
        {

            final byte[] fromKey = KeyBuilder.asSortKey(2);
            
            final byte[] toKey = KeyBuilder.asSortKey(7);
            
            // first()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.first());

                // since there was nothing visitable the cursor position NOT
                // defined
                assertFalse(cursor.isCursorPositionDefined());

                // no current key.
                assertEquals(null, cursor.currentKey());

                assertNull(cursor.tuple());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // last()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.last());

                // since there was nothing visitable the cursor position NOT
                // defined
                assertFalse(cursor.isCursorPositionDefined());

                // no current key.
                assertEquals(null, cursor.currentKey());

                assertNull(cursor.tuple());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // tuple()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.tuple());

            }

            // hasNext(), next().
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertFalse(cursor.hasNext());

                try {
                    cursor.next();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // hasPrior(), prior().
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertFalse(cursor.hasPrior());

                try {
                    cursor.prior();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // seek()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.seek(4));

                assertFalse(cursor.hasPrior());

                assertFalse(cursor.hasNext());

            }

        }

    }

    /**
     * Creates, populates and returns a {@link BTree} for
     * {@link #doOneTupleTest(AbstractBTree)}
     */
    protected BTree getOneTupleBTree() {

        BTree btree = BTree.create(new TemporaryRawStore(), new IndexMetadata(
                UUID.randomUUID()));

        btree.insert(10, "Bryan");

        return btree;

    }
    
    /**
     * Test helper for fence posts when there is only a single tuple. including
     * when attempting to visit tuples in a key range that does not overlap with
     * the tuple that is actually in the index.
     * 
     * @param btree
     */
    protected void doOneTupleTest(AbstractBTree btree) {

        /*
         * Test with no range limits.
         */
        {
            
            // first()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.first());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // last()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.last());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // tuple()
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.tuple());

            }

            // hasNext(), next().
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertTrue(cursor.hasNext());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.next());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());

                assertFalse(cursor.hasNext());

                try {
                    cursor.next();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // hasPrior(), prior().
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertTrue(cursor.hasPrior());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.prior());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.tuple());

                assertFalse(cursor.hasPrior());

                try {
                    cursor.prior();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // seek() (found)
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor
                        .seek(10));

                assertFalse(cursor.hasPrior());

                assertFalse(cursor.hasNext());

            }

            // seek() (not found before a valid tuple)
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.seek(1));

                assertEquals(KeyBuilder.asSortKey(1), cursor.currentKey());

                assertFalse(cursor.hasPrior());

                assertTrue(cursor.hasNext());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.next());

            }

            // seek() (not found after a valid tuple)
            {

                final ITupleCursor<String> cursor = newCursor(btree);

                assertNull(cursor.seek(11));

                assertTrue(cursor.hasPrior());

                assertFalse(cursor.hasNext());

                assertEquals(new TestTuple<String>(10, "Bryan"), cursor.prior());

            }

        }

        /*
         * Now use a cursor whose key-range constraint does not overlap the
         * tuple (the cursor is constrained to only visit tuples that are
         * ordered BEFORE the sole tuple actually present in the index).
         */
        {
            
            final byte[] fromKey = KeyBuilder.asSortKey(5);

            final byte[] toKey = KeyBuilder.asSortKey(9);
            
            // first()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertEquals(null, cursor.first());

                // since there was nothing visitable the cursor position NOT defined 
                assertFalse(cursor.isCursorPositionDefined());
                
                // no current key.
                assertEquals(null,cursor.currentKey());
                
                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // last()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertEquals(null, cursor.last());

                // since there was nothing visitable the cursor position NOT defined 
                assertFalse(cursor.isCursorPositionDefined());
                
                // no current key.
                assertEquals(null,cursor.currentKey());
                
                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // tuple()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.tuple());

            }

            // hasNext(), next().
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertFalse(cursor.hasNext());

                try {
                    cursor.next();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // hasPrior(), prior().
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertFalse(cursor.hasPrior());

                try {
                    cursor.prior();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // seek() (not found)
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.seek(7));

                assertEquals(KeyBuilder.asSortKey(7), cursor.currentKey());

                assertFalse(cursor.hasPrior());

                assertFalse(cursor.hasNext());

            }

        }
        
        /*
         * Now use a cursor whose key-range constraint does not overlap the
         * tuple (the cursor is constrained to only visit tuples that are
         * ordered AFTER the sole tuple actually present in the index).
         */
        {
            
            final byte[] fromKey = KeyBuilder.asSortKey(15);

            final byte[] toKey = KeyBuilder.asSortKey(19);
            
            // first()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertEquals(null, cursor.first());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // last()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertEquals(null, cursor.last());

                assertFalse(cursor.hasNext());

                assertFalse(cursor.hasPrior());

            }

            // tuple()
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.tuple());

            }

            // hasNext(), next().
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertFalse(cursor.hasNext());

                try {
                    cursor.next();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // hasPrior(), prior().
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertFalse(cursor.hasPrior());

                try {
                    cursor.prior();
                    fail("Expecting " + NoSuchElementException.class);
                } catch (NoSuchElementException ex) {
                    log.info("Ignoring expected exception: " + ex);
                }

            }

            // seek() (not found)
            {

                final ITupleCursor<String> cursor = newCursor(btree,
                        IRangeQuery.DEFAULT, fromKey, toKey);

                assertNull(cursor.seek(17));

                assertEquals(KeyBuilder.asSortKey(17), cursor.currentKey());

                assertFalse(cursor.hasPrior());

                assertFalse(cursor.hasNext());

            }

        }
        
    }
    
    /**
     * Compares two tuples for equality based on their data (flags, keys,
     * values, deleted marker, and version timestamp).
     * 
     * @param expected
     * @param actual
     */
    public static void assertEquals(ITuple expected, ITuple actual) {

        if (expected == null) {

            assertNull("Expecting a null tuple", actual);

            return;

        } else {

            assertNotNull("Not expecting a null tuple", actual);

        }

        assertEquals("flags.KEYS",
                ((expected.flags() & IRangeQuery.KEYS) != 0),
                ((actual.flags() & IRangeQuery.KEYS) != 0));

        assertEquals("flags.VALS",
                ((expected.flags() & IRangeQuery.VALS) != 0),
                ((actual.flags() & IRangeQuery.VALS) != 0));

        assertEquals("flags.DELETED",
                ((expected.flags() & IRangeQuery.DELETED) != 0), ((actual
                        .flags() & IRangeQuery.DELETED) != 0));

        assertEquals("flags", expected.flags(), actual.flags());

        assertEquals("key", expected.getKey(), actual.getKey());

        assertEquals("deleted", expected.isDeletedVersion(), actual
                .isDeletedVersion());

        if (!expected.isDeletedVersion()) {

            assertEquals("val", expected.getValue(), actual.getValue());

        }

        assertEquals("isNull", expected.isNull(), actual.isNull());

        assertEquals("timestamp", expected.getVersionTimestamp(), actual
                .getVersionTimestamp());

    }

}
