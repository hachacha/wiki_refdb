package wikitopdf.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.FontSelector;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Denis Lunev <den.lunev@gmail.com>
 */
public class PdfTitleWrapper {

    private ColumnText ct;
    private int column = 0;
    private int status = 0;
    float[] right = {70, 320};
    float[] left = {300, 550};
    public static String lastLine= "";
    public static String firstLine ="";
    public static int num=0;

    /**
     *
     * @param num
     * @param startPage
     * @throws FileNotFoundException
     * @throws DocumentException
     */
 
    public PdfTitleWrapper(int num, int startPage,String firstLine, String lastLine) throws FileNotFoundException, DocumentException {
        //Read settings
        int curPage = startPage;
        startPage = 0;
        PdfTitleWrapper.lastLine = lastLine;
        if(PdfTitleWrapper.lastLine!=""){
            new File("temp/tocVol-"+PdfTitleWrapper.num+"-"+PdfTitleWrapper.firstLine+".pdf").renameTo(new File(
                    "temp/"+String.format("%04d",PdfTitleWrapper.num)+"&&&"+PdfTitleWrapper.firstLine+"&&&"+PdfTitleWrapper.lastLine+"&&&.pdf"));
        }
        firstLine = firstLine.replaceAll("[_]"," ");//get rid of the _ for pretty printing :>
        String outputFileName = "temp/tocVol-" + num + "-"+firstLine+".pdf";

        pdfDocument = new Document(new Rectangle(432, 648));
        //(l,r,t,b)
        pdfDocument.setMargins(27f, 67.5f, 5.5f, 62.5f);
        
        

        

        pdfWriter = PdfWriter.getInstance(pdfDocument,
                new FileOutputStream(outputFileName));
        
        headerFooter = new TitlesFooter(startPage);
        pdfWriter.setPageEvent(headerFooter);
        pdfDocument.open();
        pdfDocument.setMarginMirroring(true);
        wikiFontSelector = new WikiFontSelector();
        //HeaderFooter hf =  new HeaderFooter(new Phrase("head1"), new Phrase("head2"));

        //pdfDocument.setHeader(hf);


        //openMultiColumn();
//        TitlesFooter.setCurrentLine(firstLine);
        PdfTitleWrapper.firstLine = firstLine;
        PdfTitleWrapper.num=num;
        
        
        
        


        PdfContentByte cb = pdfWriter.getDirectContent();
        ct = new ColumnText(cb);
        //ct.setIndent(20);
        
//        addPrologue();
//        newPage();
    }

    /**
     *
     * @param line
     * @throws DocumentException
     */
       public int chopLine(String line, int sizer) throws DocumentException{
        int chopStart = sizer - 3;
        int chopEnd = sizer + 2;
           
        if(line.length()<chopEnd){
            return 0;
        }
        String tempLine = line.substring(chopStart,chopEnd);
        System.out.println(tempLine);
        Character c = tempLine.charAt(0);
        if(c==' '){
          System.out.println("-2");
          return -2; 
        }
        c = tempLine.charAt(1);
        if(c==' '){
            System.out.println("-1");
          return -1; 
        }
        c = tempLine.charAt(2);
        if(c==' '){
            System.out.println("0/middle");
          return 0; 
        }
        c = tempLine.charAt(3);
        if(c==' '){
            System.out.println("1");
          return 1; 
        }
        c = tempLine.charAt(4);
        if(c==' '){
            System.out.println("2");
          return 2; 
        }
        return 0;
        
    }
       
    public int findSpace(String line, String lastLine, int sizer,boolean secondLine){
        int x = 0;
        for(x = sizer; x > 0; x--){//start the forloop going backwards from that part of the string
            Character c;
            c = line.charAt(x);
            System.out.println(c + " char");
            if(c == ' ' || c == '-')//check for last space before end
                return x;
            if(x <= 1){
                break;
            }
        }
        //can't find a space in the huge word so brute force cut it at 0
        System.out.println(x + " in findspace");
        return sizer;
    }
    
    public String longTitle(String line, int sizer) throws DocumentException{//if the title is too long
        System.out.println(line + " STARTING LINE");
        Character c;
        String lastLine="";
        String tempLine="";
        int slice_at;
        int orig_line_len = line.length();
        int cur_line_len = orig_line_len;
        int x =0;
        String separator = "\n  ";
        boolean secondLine=false;
        boolean inLine = false;
        while(inLine == false){
            System.out.println(cur_line_len + " this is current line length");
            if(secondLine==true)//if there has been one line you want to indent so sizer is -3
                sizer=sizer-3;
            if(cur_line_len <= sizer && secondLine == true){
                System.out.println(lastLine + " <--lastLine " + line + " <--line  short n scndl TRUE");
                lastLine = lastLine + separator +line;
                inLine = true;
                System.out.println(lastLine + " FULL DONE");
                break;
            }
            if(cur_line_len <= sizer && secondLine == false){
                System.out.println(lastLine + " <--lastLine " + line + " <--line  short n scndl FALSE");
                if(lastLine == "")
                    separator="";
                lastLine = lastLine + separator + line;
                inLine = true;
                System.out.println(lastLine + " &&&FULL DONE");
                break;
            }
            if(cur_line_len > sizer){//if the current line length is larger than the width of the current column
                if(cur_line_len < orig_line_len)//check if the current is less than original, that means we have iterated through before.
                    secondLine = true;
                for(int i=0;i<cur_line_len;i++){//iterate through line
                    c = line.charAt(i);//create char at last char before break
                    System.out.println(c);
                    if(i>1 && (i%sizer==0)){//whenever you hit the end of the column
                        slice_at  = findSpace(line,lastLine,sizer,secondLine);//find a space by counting backwards with the findspace function. it returns a string
                        if(secondLine==true){//if lastline has been turned into a non empty string
                            if(x>5)
                                return lastLine + "...";
                            
                            lastLine = lastLine+"\n  "+line.substring(0,slice_at);
                            line = line.substring(slice_at);
                            cur_line_len = line.length();
                            System.out.println(lastLine + " this is lastLine after chopping & secondline is TRUE");
                            break;
                        }
                        else{
                            lastLine=line.substring(0,slice_at);
                            line=line.substring(slice_at);
                            cur_line_len = line.length();
                            System.out.println(lastLine + " this is lastLine after chopping & secondline is FALSE");
                            break;
                        }
                    }
                    lastLine=lastLine+tempLine;
                    if(i==line.length()){
                        inLine = true;
                        break;
                    }
                }
            }
            
            x++;
            
            
        }

        System.out.println(lastLine + " lastLine just before return");
        System.out.println(line + " line before return");
        
        return lastLine;
        
//        boolean tooLong = false;
//        String tempLine;
//        if(line.length()<sizer){//sizer is the size of the column...
//            return line;
//        }
//        int near = chopLine(line,sizer);//determine where to cut the line at and start a new line
//        
//        lastLine=line.substring(0,sizer+near);
//        line = line.substring(sizer+near);
//        System.out.println(line.length() + " length of line now");
////        if(line.length()<5)
////            noSingleChar(line,lastLine);
//        int x = 0;
//        while(line.length()>=sizer+near){//while line is longer than 20 characters 
//                                         //IF this is the first iteration and the lines larger it'll be skipped
//           near = chopLine(line,sizer);//send off to find a whitespace and chop
//           lastLine = lastLine+"\n  "+line.substring(0,sizer+near);
//           line = line.substring(sizer+near);
//           if(line.length()<=sizer){//if the remaining line is less than the columnwidth
//               if(line.length()<5){//check to see if there will be a single character left.
//                   //find the last whitespace. 
//                   //if that whitespace and the remining  is less than 8 chars
//                   //remove new line from end of last
//               }
//               break;
//            }
//           x++;
//           if(x>=5){//check to see if there should be an added line at the end
//                    //if not then add an elipses and call it a day
//            tooLong = true;
//            break;
//           }
//           else{
//               tooLong = false;
//           }
//        }
//        if(tooLong==false){
//            if(line.length()>=1){
//                lastLine = lastLine +"\n"+"  "+line;
//                return lastLine;
//            }
//        }
//        else{
//            lastLine = lastLine+"...";
//            return lastLine;
//        }
//            
        
//        return lastLine;
    
    }
    public void writeTitle(String line) throws DocumentException {
        try {
            float widthLine = wikiFontSelector.getCommonFont().getBaseFont().getWidthPoint(line,
                    wikiFontSelector.getCommonFont().getSize());
            if (widthLine > 40) {
                line = longTitle(line,20);//do the function returning the correct string.
            }
                Phrase ph = wikiFontSelector.getTitleFontSelector().process(line);
                ph.setLeading(8);
                Paragraph pr = new Paragraph(ph);

                pr.setKeepTogether(true);//should this always be set to true?

            //if word wraps
            //this needs to be adjusted to indent wrapped words 2013
            

            if (mct.isOverflow()) {
                System.out.println("page is end  :" + pdfDocument.getPageNumber() + "  " + line);
                mct.nextColumn();
                pdfDocument.newPage();
            }

            mct.addElement(pr);
//            System.out.println(mct);
            pdfDocument.add(mct);
            headerFooter.setCurrentLine(line);

            
            //ph.setFontSize(20);
            

            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        currentTitleNum++;
    }


    /*
    public void wrline(String line)
    {
    this.
    ct.setSimpleColumn(this.right[column], 60, this.left[column], 700, 16, Element.ALIGN_JUSTIFIED);

    //Phrase element = new Phrase(line);
    Chapter chapter = new Chapter(new Paragraph(line), 1);

    ct.addElement(chapter);
    //ct.addText(chapter);
    try
    {
    status = ct.go();

    System.out.println("status: " + status);
    System.out.println("columnText: " + ColumnText.NO_MORE_COLUMN);
    }
    catch (Exception ex)
    {
    ex.printStackTrace();
    }
    }
     */
    /**
     *
     */
    public void close() {
        try {
            //pdfDocument.add(mct);
            //int i = 1;
            /*
            do
            {

            //mct.nextColumn();
            //pdfDocument.newPage();

            System.out.println("page " + i + " -- " );
            i++;
            }while(mct.isOverflow());
             */

            pdfDocument.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
    Prologue at the second page
     */
    /**
     *
     * @throws DocumentException
     */
    public final void jknewPage() throws DocumentException{
      pdfDocument.add(new Paragraph(""));
      pdfDocument.newPage();
      pdfDocument.add(new Paragraph(""));
    }
    public final void addPrologue() throws DocumentException {
        PdfContentByte cb = pdfWriter.getDirectContent();
        BaseFont times = null;

        try {
            wikiFontSelector.getTitleFontSelector().process("");
            times = wikiFontSelector.getCommonFont().getBaseFont();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        cb.beginText();
        
        cb.setFontAndSize(times, 32);
        cb.setTextMatrix(pdfDocument.right() - 130, 500);
        cb.showText("Wikipedia");
        
        cb.setFontAndSize(times, 8);
        cb.setTextMatrix(pdfDocument.right() - 50, 490);
        cb.showText("table of contents");
        
//        cb.setFontAndSize(times, 12);
//        cb.setTextMatrix(pdfDocument.right() - 200, 200);
//        cb.showText("testing - delete me");
        
        cb.endText();
               
        /*
        // adding blank page - WHY ISN'T THIS WORKING???
        cb.beginText();
        
        cb.setFontAndSize(times, 32);
        cb.setTextMatrix(pdfDocument.right() - 130, 500);
        cb.showText("blank page");
        
        cb.endText();

        pdfDocument.newPage();
*/
        
        
        
        String copyrightText = "Copyright (c) 2013 WIKIMEDIA FOUNDATION. \r\n" +
                "Permission is granted to copy, distribute and/or modify this document under the \r\n" +
                "terms of the GNU Free Documentation License, Version 1.2 or any later version \r\n" +
                "published by the Free Software Foundation; with no Invariant Sections, no \r\n" +
                "Front-Cover Texts, and no Back-Cover Texts. A copy of the license is included \r\n" +
                "in the section entitled ‚\"GNU Free Documentation License\".";

        cb.beginText();
        cb.setFontAndSize(times, 8);

        String[] textArr = copyrightText.split("\r\n");
        for (int i = 0; i < textArr.length; i++) {
            cb.setTextMatrix(pdfDocument.left() - 10, 100 - (i * 10));
            cb.showText(textArr[i]);
        }
        cb.endText();
        pdfDocument.newPage();
        cb.beginText();
        cb.setFontAndSize(times,8);
        cb.setTextMatrix(pdfDocument.left()-19,100);
        cb.showText("");
        cb.endText();
        
        
        
        
    }

    /**
     *
     */
    public void openMultiColumn() {
        mct = new MultiColumnText(60);
        mct.addRegularColumns(pdfDocument.left(),
                pdfDocument.right(), 6f, 4);

        //First page hack 
       
        for (int i = 0; i < 4; i++) {
            try {
                Phrase ph = wikiFontSelector.getTitleFontSelector().process("\n");

                mct.addElement(ph);
                pdfDocument.add(mct);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public void closeMultiColumn() {
        try {
            //pdfDocument.add(mct);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    public int getCurrentTitleNum() {
        return currentTitleNum;
    }

    /**
     *
     * @return
     */
    public int getPagesCount() {
        //return headerFooter.getPageNum();
        return pdfWriter.getCurrentPageNumber();
    }

    /**
     *
     * @return
     */
    public int getTitlesCount() {
        return headerFooter.getLineCount();
    }
    private Document pdfDocument = null;
    private FontSelector _fontSelector = null;
    private MultiColumnText mct = null;
    private PdfWriter pdfWriter;
    private int currentPageNum = 3;
    private int currentTitleNum = 0;
    private TitlesFooter headerFooter;
    private WikiFontSelector wikiFontSelector;
}
