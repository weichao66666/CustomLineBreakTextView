package io.weichao.customlinebreaktextview.widget;

import android.content.Context;
import android.content.res.TypedArray;
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

import io.weichao.customlinebreaktextview.R;

/**
 * Created by chao.wei on 2018/2/6.
 */
public class CustomLineBreakTextView extends View {
    private static final String TAG = "CustomLineBreakTextView";

    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_CENTER = 1;

    private int mGravity = GRAVITY_LEFT;
    private boolean mIncludeFontPadding = false;
    private float mTextSize = 42;
    private float mWordHorizontalMargin = 100;
    private float mWordVerticalMargin = 30;
    private int mTextColor = Color.DKGRAY;
    private float mLetterSpacing = 0;

    private TextPaint paint;
    private Paint.FontMetrics fontMetrics;
    private ArrayList<String> wordList = new ArrayList<>();
    private ArrayList<Line> lineList = new ArrayList<>();


    public CustomLineBreakTextView(Context context) {
        this(context, null);
    }

    public CustomLineBreakTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLineBreakTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CustomLineBreakTextView);
        if (attributes != null) {
            mGravity = attributes.getInt(R.styleable.CustomLineBreakTextView_gravity, mGravity);
            mIncludeFontPadding = attributes.getBoolean(R.styleable.CustomLineBreakTextView_includeFontPadding, mIncludeFontPadding);
            mTextSize = attributes.getDimension(R.styleable.CustomLineBreakTextView_textSize, mTextSize);
            mWordHorizontalMargin = attributes.getDimension(R.styleable.CustomLineBreakTextView_wordHorizontalMargin, mWordHorizontalMargin);
            mWordVerticalMargin = attributes.getDimension(R.styleable.CustomLineBreakTextView_wordVerticalMargin, mWordVerticalMargin);
            mTextColor = attributes.getColor(R.styleable.CustomLineBreakTextView_textColor, mTextColor);
            mLetterSpacing = attributes.getFloat(R.styleable.CustomLineBreakTextView_letterSpacing, mLetterSpacing);
        }

        init();
    }

    private void init() {
        paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);
    }

    public void setText(CharSequence charSequence) {
        wordList.clear();
        if (!TextUtils.isEmpty(charSequence)) {
            StringBuilder stringBuilder = null;
            for (int i = 0, length = charSequence.length(); i < length; i++) {
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
        Log.d(TAG, "onMeasure(" + widthMeasureSpec + ", " + heightMeasureSpec + ")");

        if (wordList == null || wordList.size() == 0) {
            Log.d(TAG, "wordList == null || wordList.size() == 0");
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();// 去掉 padding，实际可用的宽度
        if (heightMeasureSpec == 0) {// 避免在 scrollView 里获取不到高度
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
        }
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();// 去掉 padding，实际可用的高度
        Log.d(TAG, "widthSize:" + widthSize);
        Log.d(TAG, "heightSize:" + heightSize);
        if (widthSize <= 0 || heightSize <= 0) {// 避免无位置放置
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        lineList.clear();
        float usedWidth = 0;
        Line line = null;

        Log.d(TAG, "------------------------------------ 开始计算位置 ------------------------------------");
        for (String word : wordList) {
            float width;
            if (mLetterSpacing > 0) {
                width = getMeasuredWidthWithLetterSpacing(word);
            } else {
                width = paint.measureText(word);
            }
            usedWidth += width;
            if (usedWidth > widthSize) {
                lineList.add(line);
                line = new Line();
                if (width > widthSize) {
                    Word restWord = addWordListBySub(word, widthSize);
                    if (restWord != null) {
                        line = new Line();
                        line.wordList.add(restWord);
                        usedWidth = restWord.width;
                        usedWidth += mWordHorizontalMargin;
                    }
                } else {
                    usedWidth = width;
                    line.wordList.add(new Word(word, width, getTextHeight()));
                    usedWidth += mWordHorizontalMargin;
                }
            } else {
                if (line == null) {
                    line = new Line();
                }
                line.wordList.add(new Word(word, width, getTextHeight()));
                usedWidth += mWordHorizontalMargin;
            }
            Log.d(TAG, "width:" + width + "--" + "totalWidth:" + usedWidth);
        }

        if (!lineList.contains(line)) {
            lineList.add(line);// 把最后一行添加到集合中
        }
        Log.d(TAG, "------------------------------------ 结束计算位置 ------------------------------------");

        int totalWidth = widthSize + getPaddingLeft() + getPaddingRight();
        Log.d(TAG, "totalWidth: " + totalWidth);

        int totalHeight = 0;
        int lineListSize = lineList.size();
        totalHeight += lineListSize * getTextHeight();
        totalHeight += (lineListSize - 1) * mWordVerticalMargin;
        totalHeight += getPaddingTop();
        totalHeight += getPaddingBottom();
        Log.d(TAG, "totalHeight: " + totalHeight);

        setMeasuredDimension(totalWidth, totalHeight);
    }

    private float getMeasuredWidthWithLetterSpacing(String word) {
        int totalWidth = 0;
        char[] chars = word.toCharArray();
        for (int i = 0, length = chars.length; i < length; i++) {
            String cStr = String.valueOf(chars[i]);
            totalWidth += paint.measureText(cStr);
            if (i != length - 1) {
                totalWidth += mLetterSpacing;
            }
        }
        return totalWidth;
    }

    private Word addWordListBySub(String word, int widthSize) {
        Log.d(TAG, "addWordListBySub(" + word + ", " + widthSize + ")");
        if (!TextUtils.isEmpty(word)) {
            float width;
            if (mLetterSpacing > 0) {
                width = getMeasuredWidthWithLetterSpacing(word);
            } else {
                width = paint.measureText(word);
            }
            if (width < widthSize) {
                return new Word(word, width, getTextHeight());
            } else {
                float oneCharWidth = width / word.length();
                int index = (int) (widthSize / oneCharWidth);
                String substring = word.substring(0, index);
                float substringWidth;
                if (mLetterSpacing > 0) {
                    substringWidth = getMeasuredWidthWithLetterSpacing(substring);
                } else {
                    substringWidth = paint.measureText(substring);
                }
                int realIndex;
                String realSubstring;
                if (substringWidth < widthSize) {
                    realIndex = getMinSubstringWidthIndex(word, widthSize, index);
                    realSubstring = word.substring(0, realIndex);
                } else if (substringWidth > widthSize) {
                    realIndex = getMaxSubstringWidthIndex(word, widthSize, index);
                    realSubstring = word.substring(0, realIndex);
                } else {
                    realIndex = index;
                    realSubstring = substring;
                }
                float realWidth;
                if (mLetterSpacing > 0) {
                    realWidth = getMeasuredWidthWithLetterSpacing(realSubstring);
                } else {
                    realWidth = paint.measureText(realSubstring);
                }
                Line line = new Line();
                line.wordList.add(new Word(realSubstring, realWidth, getTextHeight()));
                lineList.add(line);
                String restSubstring = word.substring(realIndex);
                return addWordListBySub(restSubstring, widthSize);
            }
        }
        return null;
    }

    private int getMinSubstringWidthIndex(String word, int widthSize, int index) {
        Log.d(TAG, "getMinSubstringWidthIndex(" + word + ", " + widthSize + ", " + index + ")");
        for (int i = index + 1, size = word.length(); i < size; i++) {
            String substring = word.substring(i);
            float width;
            if (mLetterSpacing > 0) {
                width = getMeasuredWidthWithLetterSpacing(substring);
            } else {
                width = paint.measureText(substring);
            }
            if (width > widthSize) {
                int minSubstringWidthIndex = i - 1;
                Log.d(TAG, "getMinSubstringWidthIndex success: " + minSubstringWidthIndex + ", " + substring);
                return minSubstringWidthIndex;
            }
        }
        return index;
    }

    private int getMaxSubstringWidthIndex(String word, int widthSize, int index) {
        Log.d(TAG, "getMaxSubstringWidthIndex(" + word + ", " + widthSize + ", " + index + ")");
        for (int i = index - 1; i > 0; i--) {
            String substring = word.substring(i);
            float width;
            if (mLetterSpacing > 0) {
                width = getMeasuredWidthWithLetterSpacing(substring);
            } else {
                width = paint.measureText(substring);
            }
            if (width < widthSize) {
                int maxSubstringWidthIndex = i + 1;
                Log.d(TAG, "getMaxSubstringWidthIndex success: " + maxSubstringWidthIndex + ", " + substring);
                return maxSubstringWidthIndex;
            }
        }
        return index;
    }

    private float getTextHeight() {
        float textHeight;
        if (fontMetrics == null) {
            fontMetrics = paint.getFontMetrics();
            Log.d(TAG, "fontMetrics: " + fontMetrics.top + ", " + fontMetrics.ascent + ", " + fontMetrics.leading + ", " + fontMetrics.descent + ", " + fontMetrics.bottom);
        }
        if (mIncludeFontPadding) {
            textHeight = fontMetrics.bottom - fontMetrics.top;
        } else {
            textHeight = fontMetrics.descent - fontMetrics.ascent;
        }
        return textHeight;
    }

    private float getViewTop() {
        float top;
        if (mIncludeFontPadding) {
            top = fontMetrics.top;
        } else {
            top = fontMetrics.ascent;
        }
        return top;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw()");
        float totalHeight = getPaddingTop() - getViewTop();
        for (Line line : lineList) {
            ArrayList<Word> wordList = line.wordList;

            float marginWidth = 0;
            if (mGravity == GRAVITY_CENTER) {
                float totalUsedWidth = 0;
                for (Word word : wordList) {
                    totalUsedWidth += word.width;
                }
                totalUsedWidth += (wordList.size() - 1) * mWordHorizontalMargin;
                marginWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - totalUsedWidth) / 2;
            }

            float totalWidth = getPaddingLeft() + marginWidth;
            for (Word word : wordList) {
                if (mLetterSpacing > 0) {
                    for (char c : word.text.toCharArray()) {
                        String cStr = String.valueOf(c);
                        canvas.drawText(cStr, totalWidth, totalHeight, paint);// draw 从左下角开始
                        totalWidth += paint.measureText(cStr);
                        totalWidth += mLetterSpacing;
                    }
                } else {
                    canvas.drawText(word.text, totalWidth, totalHeight, paint);// draw 从左下角开始
                    totalWidth += word.width;
                }
                totalWidth += mWordHorizontalMargin;
            }

            totalHeight += wordList.get(0).height;
            totalHeight += mWordVerticalMargin;
        }
    }

    private class Line {
        private ArrayList<Word> wordList = new ArrayList<>();
    }

    private class Word {
        private String text;
        private float width;
        private float height;

        private Word(String text, float width, float height) {
            this.text = text;
            this.width = width;
            this.height = height;
        }
    }
}