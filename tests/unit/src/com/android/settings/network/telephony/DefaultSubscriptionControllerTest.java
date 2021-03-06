/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.network.telephony;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.android.settings.core.BasePreferenceController.CONDITIONALLY_UNAVAILABLE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.settings.network.SubscriptionUtil;
import com.android.settings.testutils.ResourcesUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class DefaultSubscriptionControllerTest {
    @Mock
    private SubscriptionManager mSubMgr;
    @Mock
    private TelecomManager mTelecomManager;

    private PreferenceScreen mScreen;
    private PreferenceManager mPreferenceManager;
    private ListPreference mListPreference;
    private Context mContext;
    private DefaultSubscriptionController mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = spy(ApplicationProvider.getApplicationContext());
        when(mContext.getSystemService(SubscriptionManager.class)).thenReturn(mSubMgr);
        when(mContext.getSystemService(TelecomManager.class)).thenReturn(mTelecomManager);

        final String key = "prefkey";
        mController = new TestDefaultSubscriptionController(mContext, key);

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        mPreferenceManager = new PreferenceManager(mContext);
        mScreen = mPreferenceManager.createPreferenceScreen(mContext);
        mListPreference = new ListPreference(mContext);
        mListPreference.setKey(key);
        mScreen.addPreference(mListPreference);
    }

    @After
    public void tearDown() {
        SubscriptionUtil.setActiveSubscriptionsForTesting(null);
    }

    @Test
    public void getAvailabilityStatus_twoSubscriptions_isAvailable() {
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(
                createMockSub(1, "sub1"),
                createMockSub(2, "sub2")));
        assertThat(mController.getAvailabilityStatus()).isEqualTo(AVAILABLE);
    }

    @Test
    public void isCallingAccountBindToSubscription_invalidAccount_withoutCrash() {
        doReturn(null).when(mTelecomManager).getPhoneAccount(any());

        mController.isCallingAccountBindToSubscription(null);
    }

    @Test
    public void getLabelFromCallingAccount_invalidAccount_emptyString() {
        doReturn(null).when(mTelecomManager).getPhoneAccount(any());

        assertThat(mController.getLabelFromCallingAccount(null)).isEqualTo("");
    }

    @Test
    public void displayPreference_twoSubscriptionsSub1Default_correctListPreferenceValues() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);

        final CharSequence entry = mListPreference.getEntry();
        final String value = mListPreference.getValue();
        assertThat(entry).isEqualTo("sub1");
        assertThat(value).isEqualTo("111");

        final CharSequence[] entries = mListPreference.getEntries();
        assertThat(entries.length).isEqualTo(3);
        assertThat(entries[0]).isEqualTo("sub1");
        assertThat(entries[1]).isEqualTo("sub2");
        assertThat(entries[2]).isEqualTo(
                ResourcesUtils.getResourcesString(mContext, "calls_and_sms_ask_every_time"));

        final CharSequence[] entryValues = mListPreference.getEntryValues();
        assertThat(entryValues.length).isEqualTo(3);
        assertThat(entryValues[0]).isEqualTo("111");
        assertThat(entryValues[1]).isEqualTo("222");
        assertThat(entryValues[2]).isEqualTo(
                Integer.toString(SubscriptionManager.INVALID_SUBSCRIPTION_ID));
    }

    @Test
    public void displayPreference_twoSubscriptionsSub2Default_correctListPreferenceValues() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub2.getSubscriptionId());

        mController.displayPreference(mScreen);

        final CharSequence entry = mListPreference.getEntry();
        final String value = mListPreference.getValue();
        assertThat(entry).isEqualTo("sub2");
        assertThat(value).isEqualTo("222");

        final CharSequence[] entries = mListPreference.getEntries();
        assertThat(entries.length).isEqualTo(3);
        assertThat(entries[0]).isEqualTo("sub1");
        assertThat(entries[1]).isEqualTo("sub2");
        assertThat(entries[2]).isEqualTo(
                ResourcesUtils.getResourcesString(mContext, "calls_and_sms_ask_every_time"));

        final CharSequence[] entryValues = mListPreference.getEntryValues();
        assertThat(entryValues.length).isEqualTo(3);
        assertThat(entryValues[0]).isEqualTo("111");
        assertThat(entryValues[1]).isEqualTo("222");
        assertThat(entryValues[2]).isEqualTo(
                Integer.toString(SubscriptionManager.INVALID_SUBSCRIPTION_ID));
    }

    @Test
    public void displayPreference_threeSubsOneIsOpportunistic_correctListPreferenceValues() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        final SubscriptionInfo sub3 = createMockSub(333, "sub3");

        // Mark sub2 as opportunistic; then it should not appear in the list of entries/entryValues.
        when(sub2.isOpportunistic()).thenReturn(true);

        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2, sub3));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(
                Arrays.asList(sub1, sub2, sub3));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);

        final CharSequence[] entries = mListPreference.getEntries();
        assertThat(entries.length).isEqualTo(3);
        assertThat(entries[0]).isEqualTo("sub1");
        assertThat(entries[1]).isEqualTo("sub3");
        assertThat(entries[2]).isEqualTo(
                ResourcesUtils.getResourcesString(mContext, "calls_and_sms_ask_every_time"));

        final CharSequence[] entryValues = mListPreference.getEntryValues();
        assertThat(entryValues.length).isEqualTo(3);
        assertThat(entryValues[0]).isEqualTo("111");
        assertThat(entryValues[1]).isEqualTo("333");
        assertThat(entryValues[2]).isEqualTo(
                Integer.toString(SubscriptionManager.INVALID_SUBSCRIPTION_ID));
    }

    @Test
    public void onPreferenceChange_prefChangedToSub2_callbackCalledCorrectly() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        mListPreference.setValue("222");
        mController.onPreferenceChange(mListPreference, "222");
        assertThat(mController.getDefaultSubscriptionId()).isEqualTo(222);
    }

    @Test
    public void onPreferenceChange_prefChangedToAlwaysAsk_callbackCalledCorrectly() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        mListPreference.setValue(Integer.toString(SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        mController.onPreferenceChange(mListPreference,
                Integer.toString(SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        assertThat(mController.getDefaultSubscriptionId()).isEqualTo(
                SubscriptionManager.INVALID_SUBSCRIPTION_ID);
    }

    @Test
    public void onPreferenceChange_prefBecomesAvailable_onPreferenceChangeCallbackNotNull() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");

        // Start with only one sub active, so the pref is not available
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        assertThat(mController.isAvailable()).isTrue();

        // Now make two subs be active - the pref should become available, and the
        // onPreferenceChange callback should be properly wired up.
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));

        mController.onSubscriptionsChanged();

        assertThat(mController.isAvailable()).isTrue();
        mListPreference.callChangeListener("222");
        assertThat(mController.getDefaultSubscriptionId()).isEqualTo(222);
    }

    @Test
    public void onSubscriptionsChanged_twoSubscriptionsDefaultChanges_selectedEntryGetsUpdated() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        assertThat(mListPreference.getEntry()).isEqualTo("sub1");
        assertThat(mListPreference.getValue()).isEqualTo("111");

        mController.setDefaultSubscription(sub2.getSubscriptionId());
        mController.onSubscriptionsChanged();
        assertThat(mListPreference.getEntry()).isEqualTo("sub2");
        assertThat(mListPreference.getValue()).isEqualTo("222");
    }

    @Test
    public void onSubscriptionsChanged_goFromTwoSubscriptionsToOne_prefDisappears() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        assertThat(mController.isAvailable()).isTrue();
        assertThat(mListPreference.isVisible()).isTrue();
        assertThat(mListPreference.isEnabled()).isTrue();

        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1));
        mController.onSubscriptionsChanged();

        assertThat(mController.isAvailable()).isTrue();
        assertThat(mListPreference.isVisible()).isTrue();
        assertThat(mListPreference.isEnabled()).isFalse();
    }

    @Test
    public void onSubscriptionsChanged_goFromOneSubscriptionToTwo_prefAppears() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        assertThat(mController.isAvailable()).isTrue();
        assertThat(mListPreference.isVisible()).isTrue();
        assertThat(mListPreference.isEnabled()).isFalse();

        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1, sub2));
        mController.onSubscriptionsChanged();

        assertThat(mController.isAvailable()).isTrue();
        assertThat(mListPreference.isVisible()).isTrue();
        assertThat(mListPreference.isEnabled()).isTrue();
    }

    @Test
    public void onSubscriptionsChanged_goFromTwoToThreeSubscriptions_listGetsUpdated() {
        final SubscriptionInfo sub1 = createMockSub(111, "sub1");
        final SubscriptionInfo sub2 = createMockSub(222, "sub2");
        final SubscriptionInfo sub3 = createMockSub(333, "sub3");
        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(Arrays.asList(sub1, sub2));
        mController.setDefaultSubscription(sub1.getSubscriptionId());

        mController.displayPreference(mScreen);
        assertThat(mListPreference.getEntries().length).isEqualTo(3);

        SubscriptionUtil.setActiveSubscriptionsForTesting(Arrays.asList(sub1, sub2, sub3));
        when(mSubMgr.getAvailableSubscriptionInfoList()).thenReturn(
                Arrays.asList(sub1, sub2, sub3));
        mController.onSubscriptionsChanged();

        assertThat(mController.isAvailable()).isTrue();
        assertThat(mListPreference.isVisible()).isTrue();
        final CharSequence[] entries = mListPreference.getEntries();
        final CharSequence[] entryValues = mListPreference.getEntryValues();
        assertThat(entries.length).isEqualTo(4);
        assertThat(entries[0].toString()).isEqualTo("sub1");
        assertThat(entries[1].toString()).isEqualTo("sub2");
        assertThat(entries[2].toString()).isEqualTo("sub3");
        assertThat(entries[3].toString()).isEqualTo(
                ResourcesUtils.getResourcesString(mContext, "calls_and_sms_ask_every_time"));
        assertThat(entryValues[0].toString()).isEqualTo("111");
        assertThat(entryValues[1].toString()).isEqualTo("222");
        assertThat(entryValues[2].toString()).isEqualTo("333");
        assertThat(entryValues[3].toString()).isEqualTo(
                Integer.toString(SubscriptionManager.INVALID_SUBSCRIPTION_ID));
    }

    private SubscriptionInfo createMockSub(int id, String displayName) {
        final SubscriptionInfo sub = mock(SubscriptionInfo.class);
        when(sub.getSubscriptionId()).thenReturn(id);
        when(sub.getDisplayName()).thenReturn(displayName);
        return sub;
    }

    private class TestDefaultSubscriptionController extends DefaultSubscriptionController {
        int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

        TestDefaultSubscriptionController(Context context, String preferenceKey) {
            super(context, preferenceKey);
        }

        @Override
        protected SubscriptionInfo getDefaultSubscriptionInfo() {
            return null;
        }

        @Override
        protected int getDefaultSubscriptionId() {
            return mSubId;
        }

        @Override
        protected void setDefaultSubscription(int subscriptionId) {
            mSubId = subscriptionId;
        }
    }
}
