package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see HeapPage#HeapPage
 */
public class HeapFile implements DbFile {

    public File f;
    public TupleDesc td;
    public int unique_id;
    // public ConcurrentHashMap<PageId, Page> pages;

    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        this.f = f;
        this.td = td;
        this.unique_id = f.getAbsoluteFile().hashCode();
        // this.pages = Pages[];

    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // TODO: some code goes here
        return this.unique_id;
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {

        // Page page =
        // Database.getCatalog().getDatabaseFile(pid.getTableId()).readPage(pid);
        HeapPage page = null;
        int offset = pid.getPageNumber() * BufferPool.getPageSize();
        byte[] bytes = new byte[BufferPool.getPageSize()];
        try {
            try (InputStream in = new FileInputStream(this.f)) {
                // page size
                while (offset < bytes.length) {
                    int result = in.read(bytes, offset, bytes.length - offset);
                    if (result == -1) {
                        throw new IllegalArgumentException("page doesn't exist");
                    }
                    offset += result;
                }
                HeapPageId pageid = (HeapPageId) pid;
                page = new HeapPage(pageid, bytes);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return page;

        // pgNum * bp.getPageSize ---> offset
        // Java library i/o ---> open file and read from that offset

        // pages.put(pid, page);
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // page.getId().pageno()

        int offset = page.getId().getPageNumber() * BufferPool.getPageSize();
        byte[] bytes = page.getPageData();

        try {
            try (OutputStream in = new FileOutputStream(this.f)) {
                // page size
                in.write(bytes, offset, bytes.length);

            }
        } catch (IOException e) {
            throw new IOException("io exception");
        }

    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        return (int) this.f.length() / BufferPool.getPageSize();
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    /*
     * Returns an iterator over all the tuples stored in this DbFile. The
     * iterator must use {@link BufferPool#getPage}, rather than
     * {@link #readPage} to iterate through the pages.
     *
     * @return an iterator over all the tuples stored in this DbFile.
     */
    public DbFileIterator iterator(TransactionId tid) {

        DbFileIterator dbiterator = new DbFileIterator() {
            int current_page = 0;
            boolean newpagestarted = false;
            boolean open = false;
            Iterator<Tuple> pageiterator;

            @Override
            public void open() throws DbException, TransactionAbortedException {
                current_page = 0;
                open = true;
            }

            @Override
            public void rewind() throws DbException, TransactionAbortedException {
                current_page = 0;
            }

            @Override
            public void close() {
                pageiterator = null;
                open = false;
            }

            @Override
            public boolean hasNext() throws DbException, TransactionAbortedException {
                if (!open) {
                    return false;
                }
                if (current_page < numPages()) {
                    if (pageiterator == null || newpagestarted) {
                        HeapPageId pid = new HeapPageId(getId(), current_page);

                        BufferPool bp = new BufferPool(numPages());
                        pageiterator = ((HeapPage) bp.getPage(tid, pid, Permissions.READ_ONLY)).iterator();
                    }
                    return pageiterator.hasNext();
                }
                throw new DbException("db exception");
            }
            // flatten out the tuples

            @Override
            public Tuple next() throws DbException, TransactionAbortedException {
                if (hasNext() && open) { // track page in hasnext
                    // table id: DbFile.getId()

                    if (this.pageiterator.hasNext()) { // if current page has more tuples

                        return this.pageiterator.next();
                    } else { // if not go to new page and check if next page has tuples
                        current_page += 1;
                        newpagestarted = true;
                        if (hasNext()) { // if there is another tuple
                            return this.pageiterator.next();
                        }
                    }
                    throw new DbException("no more tuples");

                }
                throw new NoSuchElementException("no more tuples");

            }
        };// Anonymous inner class ends here

        return dbiterator;

    }

}