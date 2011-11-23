package org.freyja.pojo;

public class Page { 
    
    /** imply if the page has previous page */ 
    private boolean hasPrePage; 
    
    /** imply if the page has next page */ 
    private boolean hasNextPage; 
        
    /** the number of every page */ 
    private int everyPage; 
    
    /** the total page number */ 
    private long totalPage; 
        
    /** the number of current page */ 
    private int currentPage; 
    
    /** the begin index of the records by the current query */ 
    private int beginIndex; 
    
    
    /** The default constructor */ 
    public Page(){ 
        
    } 
    
    /** construct the page by everyPage 
     * @param everyPage 
     * */ 
    public Page(int everyPage){ 
        this.everyPage = everyPage; 
    } 
    
    /** The whole constructor */ 
    public Page(boolean hasPrePage, boolean hasNextPage,  
                    int everyPage, long totalPage, 
                    int currentPage, int beginIndex) { 
        this.hasPrePage = hasPrePage; 
        this.hasNextPage = hasNextPage; 
        this.everyPage = everyPage; 
        this.totalPage = totalPage; 
        this.currentPage = currentPage; 
        this.beginIndex = beginIndex; 
    } 

    /** 
     * @return 
     * Returns the beginIndex. 
     */ 
    public int getBeginIndex() { 
        return beginIndex; 
    } 
    
    /** 
     * @param beginIndex 
     * The beginIndex to set. 
     */ 
    public void setBeginIndex(int beginIndex) { 
        this.beginIndex = beginIndex; 
    } 
    
    /** 
     * @return 
     * Returns the currentPage. 
     */ 
    public int getCurrentPage() { 
        return currentPage; 
    } 
    
    /** 
     * @param currentPage 
     * The currentPage to set. 
     */ 
    public void setCurrentPage(int currentPage) { 
        this.currentPage = currentPage; 
    } 
    
    /** 
     * @return 
     * Returns the everyPage. 
     */ 
    public int getEveryPage() { 
        return everyPage; 
    } 
    
    /** 
     * @param everyPage 
     * The everyPage to set. 
     */ 
    public void setEveryPage(int everyPage) { 
        this.everyPage = everyPage; 
    } 
    
    /** 
     * @return 
     * Returns the hasNextPage. 
     */ 
    public boolean getHasNextPage() { 
        return hasNextPage; 
    } 
    
    /** 
     * @param hasNextPage 
     * The hasNextPage to set. 
     */ 
    public void setHasNextPage(boolean hasNextPage) { 
        this.hasNextPage = hasNextPage; 
    } 
    
    /** 
     * @return 
     * Returns the hasPrePage. 
     */ 
    public boolean getHasPrePage() { 
        return hasPrePage; 
    } 
    
    /** 
     * @param hasPrePage 
     * The hasPrePage to set. 
     */ 
    public void setHasPrePage(boolean hasPrePage) { 
        this.hasPrePage = hasPrePage; 
    } 
    
    /** 
     * @return Returns the totalPage. 
     * 
     */ 
    public long getTotalPage() { 
        return totalPage; 
    } 
    
    /** 
     * @param totalPage 
     * The totalPage to set. 
     */ 
    public void setTotalPage(long totalPage) { 
        this.totalPage = totalPage; 
    } 
    
} 

