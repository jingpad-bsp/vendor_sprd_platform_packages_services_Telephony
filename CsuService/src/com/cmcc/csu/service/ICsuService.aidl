// ICsuService.aidl
package com.cmcc.csu.service;

import com.cmcc.csu.service.model.ConfigResponse;

/**
 * Interface between CS SDK and CSU
 */
interface ICsuService {
    /**
     * Acquire equipment capability
     *
     * @param privateKey Application signature
     * @param packageName Application package name
     *
     * @return ConfigResponse
     */
    ConfigResponse getConfiguration(in String privateKey, in String packageName);

    /**
     * Update capability configuration
     *
     * @param privateKey Application signature
     * @param packageName Application package name
     * @param configInfoUpdate Application package name
     *
     * @return ConfigResponse
     */
    ConfigResponse postConfigurationUpdate(in String privateKey, in String packageName, in String configInfoUpdate);

}