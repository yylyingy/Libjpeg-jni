//
// Created by Yangyl on 2016/11/13.
//

#ifndef TESTLIBJPEG_NATIVE_CONFIG_H
#define TESTLIBJPEG_NATIVE_CONFIG_H
extern "C" {
#include "setjmp.h"
#include "jpeglib.h"
#include "jconfig.h"
//#include "jmorecfg.h"
#include "../../../libs/cdjpeg.h"


struct my_error_mgr {
    struct jpeg_error_mgr pub;
    jmp_buf setjmp_buffer;        //for return to caller
};
typedef struct my_error_mgr *my_error_ptr;
}
#endif //TESTLIBJPEG_NATIVE_CONFIG_H
