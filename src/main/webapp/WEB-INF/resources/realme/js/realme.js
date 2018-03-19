var RealMe = RealMe || function (t, i, n) {
        var o = navigator.userAgent.match(/(IEMobile\/9.0)/), e = /\bMSIE 6/.test(navigator.userAgent) && !i.opera;
        if (e)return !1;
        var a = {
            cacheElements: function () {
                this.$container = $(".realme_widget"), this.$trigger = $(".whats_realme", this.$container), this.$modal = $(".realme_popup", this.$container)
            }, init: function () {
                this.cacheElements(), "ontouchstart"in t || null !== o ? (this.$container.addClass("touch"), this.popup_window()) : (this.$container.addClass("no_touch"), this.bind_no_touch()), this.bind_click_login()
            }, bind_no_touch: function () {
                this.$trigger.on("click", function (t) {
                    t.preventDefault()
                })
            }, show_popup: function () {
                this.$modal.addClass("active")
            }, hide_popup: function () {
                this.$modal.removeClass("active")
            }, popup_window: function () {
                var t = this;
                this.$trigger.click(function (i) {
                    this.$modal.hasClass("active") ? t.hide_popup() : t.show_popup(), i.stopPropagation()
                }), this.$trigger.click(function () {
                    return !1
                })
            }, bind_click_login: function () {
                var loginButton = $('#realme_login');
                var shareInput = $('#realme_share_input');
                var submitButton = $('#realme_contact_submit');

                if (submitButton[0] && shareInput[0].value === '') {
                    submitButton[0].disabled = true;
                }
                if (shareInput[0]) {
                    loginButton.on('click', function (event) {
                        event.preventDefault();
                        loginButton[0].classList.add('realme_hide');
                        shareInput[0].classList.remove('realme_hide');
                        submitButton[0].classList.remove('realme_hide');
                        shareInput[0].focus();
                    });
                    shareInput.on('change', function () {
                        submitButton[0].disabled = shareInput.value === "";
                    });
                }
            }
        };
        return n(t).ready(function () {
            a.init()
        }), a
    }(document, window, jQuery);