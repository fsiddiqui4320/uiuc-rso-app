package edu.illinois.cs.cs124.ay2024.mp.test.helpers;

import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.common.truth.Truth.assertWithMessage;

import android.view.View;
import android.widget.SearchView;
import android.widget.ToggleButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers;
import org.hamcrest.Matcher;

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 *
 * The helper methods in this file assist with checking UI components during testing.
 */
public class Views {
  /** Count the number of items in a RecyclerView. */
  public static ViewAssertion countRecyclerView(int expected) {
    return (v, noViewFoundException) -> {
      if (noViewFoundException != null) {
        throw noViewFoundException;
      }
      RecyclerView view = (RecyclerView) v;
      RecyclerView.Adapter<?> adapter = view.getAdapter();
      assertWithMessage("View adapter should not be null").that(adapter).isNotNull();
      assertWithMessage("Adapter should have " + expected + " items")
          .that(adapter.getItemCount())
          .isEqualTo(expected);
    };
  }

  /** Search for text in a SearchView component. */
  public static ViewAction searchFor(String query) {
    return searchFor(query, false);
  }

  /** ViewAction used to support searching for text in a SearchView component. */
  public static ViewAction searchFor(String query, boolean submit) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        if (submit) {
          return "Set query to " + query + " and submit";
        } else {
          return "Set query to " + query + " but don't submit";
        }
      }

      @Override
      public void perform(UiController uiController, View view) {
        SearchView searchView = (SearchView) view;
        searchView.setQuery(query, submit);
      }
    };
  }

  /** ViewAssertion for testing ToggleButton components. */
  public static ViewAssertion isChecked(boolean checked) {
    return (view, noViewFoundException) -> {
      assertWithMessage("Should have found view").that(noViewFoundException).isNull();
      assertWithMessage("View should be a ToggleButton")
          .that(view)
          .isInstanceOf(ToggleButton.class);

      ToggleButton toggleButton = (ToggleButton) view;
      String message;
      if (checked) {
        message = "ToggleButton should be checked";
      } else {
        message = "ToggleButton should not be checked";
      }
      assertWithMessage(message).that(toggleButton.isChecked()).isEqualTo(checked);
    };
  }

  /** ViewAction for checking or unchecking ToggleButton components. */
  public static ViewAction setChecked(boolean checked) {
    return actionWithAssertions(
        new ViewAction() {
          @Override
          public Matcher<View> getConstraints() {
            return ViewMatchers.isAssignableFrom(ToggleButton.class);
          }

          @Override
          public String getDescription() {
            return "Custom view action to check or uncheck ToggleButton";
          }

          @Override
          public void perform(UiController uiController, View view) {
            ToggleButton toggleButton = (ToggleButton) view;
            toggleButton.setChecked(checked);
          }
        });
  }
}

// md5: ef560ced92a08405114e2bb44a4d9718 // DO NOT REMOVE THIS LINE
