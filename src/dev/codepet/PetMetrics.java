package dev.codepet;

/** Lightweight lexical metric: excludes whitespace and standalone comment lines, not a semantic code score. */
final class PetMetrics {
    record Counts(int characters,int lines) {}
    static Counts count(CharSequence text) {
        int characters=0,lines=0,start=0;
        for(int end=0;end<=text.length();end++) {
            if(end<text.length()&&text.charAt(end)!='\n')continue;
            int first=start;while(first<end&&Character.isWhitespace(text.charAt(first)))first++;
            boolean comment=first<end&&(text.charAt(first)=='#'||text.charAt(first)=='*'||
                    (first+1<end&&((text.charAt(first)=='/'&&(text.charAt(first+1)=='/'||text.charAt(first+1)=='*'))||
                            (text.charAt(first)=='-'&&text.charAt(first+1)=='-'))));
            if(!comment){int count=0;for(int i=start;i<end;i++)if(!Character.isWhitespace(text.charAt(i)))count++;characters+=count;if(count>10)lines++;}
            start=end+1;
        }
        return new Counts(characters,lines);
    }
}
