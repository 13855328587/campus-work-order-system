package com.example.workorder.service;

import com.example.workorder.vo.CaptchaVO;

public interface CaptchaService {
    CaptchaVO generate();

    void validate(String captchaId, String captchaCode);
}
