package com.example.liftvectr.activities;

import org.junit.Test;

public class AddExerciseActivityTest {
    @Test
    public void testNotInEmulationMode() {
        assert(!AddExerciseActivity.emulationMode);
    }
}