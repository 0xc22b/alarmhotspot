package com.wit.alarmhotspot.model.test;

import com.wit.alarmhotspot.model.TransferObj;

import android.test.AndroidTestCase;

public class TransferObjTest extends AndroidTestCase {

    public void testDidExceed() {
        assertEquals(false, TransferObj.didExceed(5411200, 1512200, 6321100,
                1522200, 20000000));
        assertEquals(false, TransferObj.didExceed(5411200, 1512200, 6321100,
                1522200, 50000000));   
        assertEquals(true, TransferObj.didExceed(5411200, 1512200, 6321100,
                1522200, 500000));
    }
    
    public void testGetAmountTransferred() {
        assertEquals(919900l, TransferObj.getAmountTransferred(5411200, 1512200,
                6321100, 1522200));
    }
    
    public void testRxTxGetAmountToLimit() {
        assertEquals(19080100l, TransferObj.getAmountToLimit(5411200, 1512200,
                6321100, 1522200, 20000000l));
    }
}
