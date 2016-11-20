#include <jni.h>
#include <string>
#include "native-config.h"


extern "C" {
jstring
Java_com_yangyl_libjpeg_JpegDecCompress_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    struct jpeg_compress_struct jcs;
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

void my_error_exit (j_common_ptr cinfo)
{
    my_error_ptr myerr = (my_error_ptr) cinfo->err;

    (*cinfo->err->output_message) (cinfo);

    longjmp(myerr->setjmp_buffer, 1);
}

jbyteArray
Java_com_yangyl_libjpeg_JpegDecCompress_decodeJpeg(
        JNIEnv *env,
        jobject ,
        jbyteArray jpeg_buff_in,
        jint jpeg_size
        ){
    struct jpeg_decompress_struct cinfo;
    struct my_error_mgr jerr;
    unsigned char *jpeg_buffer=NULL;
    unsigned char *rgb_buffer;
    JSAMPARRAY buffer;
    int row_stride = 0;
    unsigned char* tmp_buffer = NULL;
    size_t rgb_size;

    if((env)->GetArrayLength(jpeg_buff_in)<jpeg_size){
        printf("Source buffer is not large enough");
    }

    jpeg_buffer= (unsigned char *) (env)->GetByteArrayElements(jpeg_buff_in, 0);

    if (jpeg_buffer == NULL)
    {
        printf("no jpeg buffer here.\n");
        return NULL;
    }

    cinfo.err = jpeg_std_error(&jerr.pub);
    jerr.pub.error_exit = my_error_exit;

    if (setjmp(jerr.setjmp_buffer))
    {
        jpeg_destroy_decompress(&cinfo);
        return NULL;
    }

    jpeg_create_decompress(&cinfo);

    jpeg_mem_src(&cinfo, jpeg_buffer, (unsigned long) jpeg_size);

    jpeg_read_header(&cinfo, TRUE);

    cinfo.out_color_space = JCS_RGB; //JCS_YCbCr;  // 设置输出格式

    jpeg_start_decompress(&cinfo);

    row_stride = cinfo.output_width * cinfo.output_components;
   // *width = cinfo.output_width;
  //  *height = cinfo.output_height;

    rgb_size = row_stride * cinfo.output_height; // 总大小
    rgb_buffer = (unsigned char *) malloc(rgb_size);
    jbyteArray rgb_byte = env->NewByteArray(rgb_size) ;

//    rgb_byte = env->NewByteArray(rgb_size);
//    if (*size < rgb_size)
//    {
//        printf("rgb buffer to small, we need %d but has only: %d\n", rgb_size, *size);
//    }
//
//    *size = rgb_size;

    buffer = (*cinfo.mem->alloc_sarray)((j_common_ptr) &cinfo, JPOOL_IMAGE, row_stride, 1);

    printf("debug--:\nrgb_size: %d, size: %d w: %d h: %d row_stride: %d \n", rgb_size,
           cinfo.image_width*cinfo.image_height*3,
           cinfo.image_width,
           cinfo.image_height,
           row_stride);
    tmp_buffer = (unsigned char *) rgb_buffer;
    while (cinfo.output_scanline < cinfo.output_height) // 解压每一行
    {
        jpeg_read_scanlines(&cinfo, buffer, 1);
        // 复制到内存
        memcpy(tmp_buffer, buffer[0], row_stride);
        tmp_buffer += row_stride;
    }

    jpeg_finish_decompress(&cinfo);
    jpeg_destroy_decompress(&cinfo);
    env->SetByteArrayRegion(rgb_byte, 0, rgb_size, (const jbyte *) rgb_buffer);
    free((void *) rgb_buffer);
    env->ReleaseByteArrayElements(jpeg_buff_in, (jbyte *) jpeg_buffer, 0);
    return rgb_byte;
}
}
