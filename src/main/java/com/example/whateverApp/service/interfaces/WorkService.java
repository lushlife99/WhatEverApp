package com.example.whateverApp.service.interfaces;

import com.example.whateverApp.model.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

public interface WorkService {

    Work update(Work work);
    Work delete(Long workId);
    Work get(HttpServletRequest request);
    Work success(Long workId);
}
