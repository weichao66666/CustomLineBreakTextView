package io.weichao.customlinebreaktextview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao.wei on 2018/2/6.
 */
public class CustomLineBreakTextView extends View {
    private static final String TAG = "CustomLineBreakTextView";

    public static final int CENTER = 0;

    private TextPaint paint;

    private int mGravity = CENTER;

    private ArrayList<String> wordList = new ArrayList<>();
    private ArrayList<Line> lineList = new ArrayList<>();

    private int wordHorizontalMargin = 100;
    private int wordVerticalMargin = 30;
    private float textHeight = 42;

    public CustomLineBreakTextView(Context context) {
        this(context, null);
    }

    public CustomLineBreakTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLineBreakTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textHeight);
    }

    public void setText(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            wordList.clear();
            StringBuilder stringBuilder = null;
            int length = charSequence.length();
            for (int i = 0; i < length; i++) {
                char c = charSequence.charAt(i);
//                Log.d(TAG, "charSequence.charAt(" + i + "): " + c);
                if (c != '-' && (c < 'A' || (c > 'Z' && c < 'a') || c > 'z')) {
                    if (stringBuilder != null) {
                        wordList.add(stringBuilder.toString());
                        stringBuilder = null;
                    }
                } else {
                    if (stringBuilder == null) {
                        stringBuilder = new StringBuilder();
                    }
                    stringBuilder.append(c);
                }
            }
            if (stringBuilder != null) {
                wordList.add(stringBuilder.toString().trim());
            }
            for (int i = 0, size = wordList.size(); i < size; i++) {
                Log.d(TAG, "word[" + i + "]: " + wordList.get(i));
            }
        } else {
            Log.e(TAG, "TextUtils.isEmpty(charSequence)");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure()");
        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();// 去掉 padding，实际可用的宽度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();// 去掉 padding，实际可用的高度
        Log.d(TAG, "widthSize:" + widthSize);
        Log.d(TAG, "heightSize:" + heightSize);

        lineList.clear();
        float usedWidth = 0;
        Line line = null;
        for (String word : wordList) {
            float width = paint.measureText(word);
            usedWidth += width;
            if (usedWidth > widthSize) {
                lineList.add(line);
                line = new Line();
                if (width > widthSize) {
                    Word restWord = addWordListBySub(lineList, paint, word, widthSize);
                    if (restWord != null) {
                        line = new Line();
                        line.getWordList().add(restWord);
                        usedWidth = restWord.width;
                        usedWidth += wordHorizontalMargin;
                    }
                } else {
                    usedWidth = width;
                    line.getWordList().add(new Word(word, width, paint.getTextSize()));
                    usedWidth += wordHorizontalMargin;
                }
            } else {
                if (line == null) {
                    line = new Line();
                }
                line.getWordList().add(new Word(word, width, paint.getTextSize()));
                usedWidth += wordHorizontalMargin;
            }
            Log.d(TAG, "width:" + width + "--" + "totalWidth:" + usedWidth);
        }

        if (!lineList.contains(line)) {
            lineList.add(line);// 把最后一行添加到集合中
        }

        int totalWidth = widthSize + getPaddingLeft() + getPaddingRight();
        Log.d(TAG, "totalWidth: " + totalWidth);

        int totalHeight = 0;
        int lineListSize = lineList.size();
        for (int i = 0; i < lineListSize; i++) {
            totalHeight += lineList.get(i).getWordList().get(0).height;
        }
        totalHeight += (lineListSize - 1) * wordVerticalMargin;
        totalHeight += getPaddingTop();
        totalHeight += getPaddingBottom();
        Log.d(TAG, "totalHeight: " + totalHeight);

        setMeasuredDimension(totalWidth, totalHeight);
    }

    private Word addWordListBySub(List<Line> lineList, TextPaint paint, String word, int widthSize) {
        Log.d(TAG, "addWordListBySub(" + word + ", " + widthSize + ")");
        if (!TextUtils.isEmpty(word)) {
            float width = paint.measureText(word);
            if (width < widthSize) {
                return new Word(word, width, paint.getTextSize());
            } else {
                float oneCharWidth = width / word.length();
                int index = (int) (widthSize / oneCharWidth);
                String substring = word.substring(0, index);
                float substringWidth = paint.measureText(substring);
                int realIndex;
                String realSubstring;
                if (substringWidth < widthSize) {
                    realIndex = getMinSubstringWidthIndex(paint, word, widthSize, index);
                    realSubstring = word.substring(0, realIndex);
                } else if (substringWidth > widthSize) {
                    realIndex = getMaxSubstringWidthIndex(paint, word, widthSize, index);
                    realSubstring = word.substring(0, realIndex);
                } else {
                    realIndex = index;
                    realSubstring = substring;
                }
                float realWidth = paint.measureText(realSubstring);
                Line line = new Line();
                line.getWordList().add(new Word(realSubstring, realWidth, paint.getTextSize()));
                lineList.add(line);
                String restSubstring = word.substring(realIndex + 1);
                return addWordListBySub(lineList, paint, restSubstring, widthSize);
            }
        }
        return null;
    }

    private int getMinSubstringWidthIndex(TextPaint paint, String word, int widthSize, int index) {
        Log.d(TAG, "getMinSubstringWidthIndex(" + word + ", " + widthSize + ", " + index + ")");
        for (int i = index + 1, size = word.length(); i < size; i++) {
            String substring = word.substring(i);
            float width = paint.measureText(substring);
            if (width > widthSize) {
                int minSubstringWidthIndex = i - 1;
                Log.d(TAG, "getMinSubstringWidthIndex success: " + minSubstringWidthIndex + ", " + substring);
                return minSubstringWidthIndex;
            }
        }
        return index;
    }

    private int getMaxSubstringWidthIndex(TextPaint paint, String word, int widthSize, int index) {
        Log.d(TAG, "getMaxSubstringWidthIndex(" + word + ", " + widthSize + ", " + index + ")");
        for (int i = index - 1; i > 0; i--) {
            String substring = word.substring(i);
            float width = paint.measureText(substring);
            if (width < widthSize) {
                int maxSubstringWidthIndex = i + 1;
                Log.d(TAG, "getMaxSubstringWidthIndex success: " + maxSubstringWidthIndex + ", " + substring);
                return maxSubstringWidthIndex;
            }
        }
        return index;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw()");
        float totalHeight = getPaddingTop() + textHeight;
        for (Line line : lineList) {
            ArrayList<Word> wordList = line.getWordList();

            float marginWidth = 0;
            if (mGravity == CENTER) {
                float totalUsedWidth = 0;
                for (Word word : wordList) {
                    totalUsedWidth += word.width;
                }
                totalUsedWidth += (wordList.size() - 1) * wordHorizontalMargin;
                marginWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - totalUsedWidth) / 2;
            }

            float totalWidth = getPaddingLeft() + marginWidth;
            for (Word word : wordList) {
                canvas.drawText(word.text, totalWidth, totalHeight, paint);// draw 从左下角开始
                totalWidth += word.width;
                totalWidth += wordHorizontalMargin;
            }

            totalHeight += wordList.get(0).height;
            totalHeight += wordVerticalMargin;
        }
    }

    private class Line {
        private ArrayList<Word> wordList = new ArrayList<>();

        private ArrayList<Word> getWordList() {
            return wordList;
        }

        private void setWordList(ArrayList<Word> wordList) {
            this.wordList = wordList;
        }
    }

    private class Word {
        private String text;
        private float width;
        private float height;

        public Word() {
        }

        private Word(String text, float width, float height) {
            this.text = text;
            this.width = width;
            this.height = height;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }
    }
}
