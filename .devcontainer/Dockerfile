FROM eclipse-temurin:21-jdk

LABEL maintainer="Simon Marquis <contact@simon-marquis.fr>"
LABEL description="A Docker image for Android devs."
LABEL version="1.0.0"

ARG CMD_LINE_VERSION=12700392
ARG ANDROID_VERSION=35
ARG ANDROID_BUILD_TOOLS_VERSION=35.0.1

ENV ANDROID_HOME /opt/android
ENV ANDROID_SDK_ROOT /opt/android
ENV PATH $PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

RUN apt-get update \
    && apt-get install -y --no-install-recommends git curl unzip

# Install Android Command Line Tools
# https://developer.android.com/studio#command-line-tools-only
RUN curl https://dl.google.com/android/repository/commandlinetools-linux-${CMD_LINE_VERSION}_latest.zip -o /tmp/cmd-tools.zip \
    && unzip -d $ANDROID_HOME /tmp/cmd-tools.zip \
    && mv $ANDROID_HOME/cmdline-tools $ANDROID_HOME/latest \
    && mkdir $ANDROID_HOME/cmdline-tools \
    && mv $ANDROID_HOME/latest $ANDROID_HOME/cmdline-tools/latest

# Install Android SDKs
RUN yes | sdkmanager --licenses --sdk_root="$ANDROID_HOME" \
    && sdkmanager --install --sdk_root="$ANDROID_HOME" \
        "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
        "platforms;android-${ANDROID_VERSION}" \
        "platform-tools" \
        "extras;android;m2repository" \
        "extras;google;m2repository" \
        "cmdline-tools;latest" \
    && sdkmanager --update --sdk_root="$ANDROID_HOME" \
    && sdkmanager --list

# Clean up
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
    apt-get autoremove -y && \
    apt-get clean
