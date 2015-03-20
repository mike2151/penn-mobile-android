package com.pennapps.labs.pennmobile.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pennapps.labs.pennmobile.R;
import com.pennapps.labs.pennmobile.classes.Course;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RegistrarAdapter extends ArrayAdapter<Course> {
    private final LayoutInflater inflater;
    private List<Course> courses;

    public RegistrarAdapter(Context context, int layout, List<Course> courses) {
        super(context, layout, courses);
        this.courses = courses;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Course course = getItem(position);
        String courseName = course.course_department +
                String.format("%03d", course.course_number) + " " +
                String.format("%03d", course.section_number);
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.registrar_list_item, parent, false);
            holder = new ViewHolder(view, course);
            view.setTag(holder);
        }

        holder.course = course;

        Spannable courseCode = new SpannableString(courseName);
        courseCode.setSpan(
                new ForegroundColorSpan(view.getResources().getColor(R.color.secondary_text_default_material_light)),
                courseCode.length() - 3,
                courseCode.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        courseCode.setSpan(new StyleSpan(Typeface.BOLD), 0, courseCode.length() - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.courseId.setText(courseCode);
        try {
            holder.courseInstr.setText(course.instructors.get(0).name);
            holder.courseInstr.setTextColor(Color.BLACK);
        } catch (IndexOutOfBoundsException e) {
            holder.courseInstr.setText(getContext().getString(R.string.professor_missing));
            holder.courseInstr.setTextColor(Color.parseColor("#4a000000"));
        }
        holder.courseTitle.setText(course.course_title);
        holder.courseActivity.setText(course.activity);

        return view;
    }

    public static class ViewHolder {
        @InjectView(R.id.course_id_text) TextView courseId;
        @InjectView(R.id.course_instr_text) TextView courseInstr;
        @InjectView(R.id.course_title_text) TextView courseTitle;
        @InjectView(R.id.course_activity) TextView courseActivity;
        public Course course;

        public ViewHolder(View view, Course course) {
            this.course = course;
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public int getCount() {
        return courses != null ? courses.size() : 0;
    }
}